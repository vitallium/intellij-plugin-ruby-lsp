package com.github.vitallium.rubylsp.commands

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.ConfigurationFromContext
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.ParametersList
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiElement
import com.redhat.devtools.lsp4ij.commands.LSPCommand
import com.redhat.devtools.lsp4ij.commands.LSPCommandAction
import java.nio.file.Paths

class RubyLspRunTestCommand : LSPCommandAction() {
    private val logger = thisLogger()

    override fun commandPerformed(command: LSPCommand, e: AnActionEvent) {
        val project = e.project ?: return
        val arguments = command.originalArguments ?: command.arguments
        val parsedArgs = RubyLspTestCommandArgs.from(arguments) ?: run {
            logger.warn("Ruby LSP run test: missing or invalid command arguments")
            notifyWarning(project, "Ruby LSP: unable to run test (missing command arguments).")
            return
        }

        when (val settings = findRunConfiguration(project, parsedArgs)) {
            RunnerSettingsResult.MissingFile -> notifyWarning(project, "Ruby LSP: test file not found.")
            RunnerSettingsResult.MissingPsi -> notifyWarning(project, "Ruby LSP: unable to resolve PSI for test file.")
            RunnerSettingsResult.MissingDocument -> notifyWarning(project, "Ruby LSP: unable to resolve document for test file.")
            RunnerSettingsResult.NoConfiguration -> {
                if (!runCommandFallback(project, parsedArgs)) {
                    notifyWarning(project, "Ruby LSP: no test run configuration found at this location.")
                }
            }
            is RunnerSettingsResult.Found -> runConfiguration(project, settings.settings)
        }
    }

    private fun findRunConfiguration(
        project: Project,
        args: RubyLspTestCommandArgs
    ): RunnerSettingsResult = ReadAction.compute<RunnerSettingsResult, RuntimeException> {
        val file = resolveFile(project, args.path) ?: return@compute RunnerSettingsResult.MissingFile
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return@compute RunnerSettingsResult.MissingPsi
        val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
            ?: FileDocumentManager.getInstance().getDocument(file)
            ?: return@compute RunnerSettingsResult.MissingDocument

        val element = findElementAt(document, psiFile, args.startLine, args.startColumn)
        val context = ConfigurationContext(element)
        val settings = context.configuration
            ?: context.createConfigurationsFromContext()
                ?.sortedWith(ConfigurationFromContext.COMPARATOR)
                ?.firstOrNull()
                ?.configurationSettings

        if (settings == null) RunnerSettingsResult.NoConfiguration else RunnerSettingsResult.Found(settings)
    }

    private fun runConfiguration(project: Project, settings: RunnerAndConfigurationSettings) {
        ApplicationManager.getApplication().invokeLater {
            val runManager = RunManager.getInstance(project)
            settings.isTemporary = true
            runManager.setTemporaryConfiguration(settings)
            runManager.selectedConfiguration = settings
            ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
        }
    }

    private fun resolveFile(project: Project, path: String): com.intellij.openapi.vfs.VirtualFile? {
        val normalizedPath = when {
            path.startsWith("file://") -> VfsUtilCore.urlToPath(path)
            else -> path
        }

        val fileSystem = LocalFileSystem.getInstance()
        return fileSystem.findFileByPath(normalizedPath)
            ?: project.basePath?.let { base ->
                fileSystem.findFileByPath(Paths.get(base, normalizedPath).toString())
            }
    }

    private fun runCommandFallback(project: Project, args: RubyLspTestCommandArgs): Boolean {
        val commandString = args.command?.trim().orEmpty()
        if (commandString.isBlank()) {
            return false
        }

        val commandLine = buildCommandLine(commandString) ?: return false

        ApplicationManager.getApplication().invokeLater {
            try {
                val workDir = project.basePath?.let { base ->
                    LocalFileSystem.getInstance().findFileByPath(base)
                } ?: resolveFile(project, args.path)?.parent
                if (workDir != null) {
                    commandLine.withWorkDirectory(workDir.path)
                }

                val processHandler = OSProcessHandler(commandLine)
                val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
                console.attachToProcess(processHandler)

                val title = args.name?.let { "Ruby LSP: $it" } ?: "Ruby LSP: Test Command"
                val descriptor = RunContentDescriptor(console, processHandler, console.component, title)
                RunContentManager.getInstance(project)
                    .showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor)

                processHandler.startNotify()
            } catch (exception: Exception) {
                logger.warn("Ruby LSP run test: failed to execute fallback command", exception)
                notifyWarning(project, "Ruby LSP: failed to execute test command.")
            }
        }

        return true
    }

    private fun buildCommandLine(commandString: String): GeneralCommandLine? {
        if (requiresShell(commandString)) {
            return shellCommandLine(commandString)
        }

        val commandLineParts = ParametersList.parse(commandString).toMutableList()
        if (commandLineParts.isEmpty()) {
            return null
        }

        val environment = mutableMapOf<String, String>()
        while (commandLineParts.isNotEmpty()) {
            val assignment = parseEnvAssignment(commandLineParts.first()) ?: break
            environment[assignment.first] = assignment.second
            commandLineParts.removeAt(0)
        }

        if (commandLineParts.isEmpty()) {
            return null
        }

        return GeneralCommandLine(commandLineParts).apply {
            if (environment.isNotEmpty()) {
                withEnvironment(environment)
            }
        }
    }

    private fun parseEnvAssignment(token: String): Pair<String, String>? {
        // TODO(vitallium): Is there a built-in function to parse environment variable assignments?
        val separatorIndex = token.indexOf('=')
        if (separatorIndex <= 0) return null
        val key = token.substring(0, separatorIndex)
        if (!key.matches(Regex("[A-Za-z_][A-Za-z0-9_]*"))) return null
        return key to token.substring(separatorIndex + 1)
    }

    private fun requiresShell(commandString: String): Boolean {
        return listOf("|", "&&", "||", ";", ">", "<", "`", "$(", "\n", "\r").any { commandString.contains(it) }
    }

    private fun shellCommandLine(commandString: String): GeneralCommandLine {
        return if (SystemInfo.isWindows) {
            GeneralCommandLine("cmd.exe", "/c", commandString)
        } else {
            GeneralCommandLine("/bin/sh", "-lc", commandString)
        }
    }

    private fun findElementAt(
        document: com.intellij.openapi.editor.Document,
        psiFile: com.intellij.psi.PsiFile,
        line: Int?,
        column: Int?
    ): PsiElement {
        val safeLine = (line ?: 0).coerceIn(0, maxOf(0, document.lineCount - 1))
        val lineStart = document.getLineStartOffset(safeLine)
        val lineEnd = document.getLineEndOffset(safeLine)
        val safeColumn = (column ?: 0).coerceAtLeast(0)
        val offset = (lineStart + safeColumn).coerceIn(lineStart, lineEnd)
        return psiFile.findElementAt(offset) ?: psiFile
    }

    private fun notifyWarning(project: Project, message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("RubyLsp")
            .createNotification(message, NotificationType.WARNING)
            .notify(project)
    }

    private sealed class RunnerSettingsResult {
        object MissingFile : RunnerSettingsResult()
        object MissingPsi : RunnerSettingsResult()
        object MissingDocument : RunnerSettingsResult()
        object NoConfiguration : RunnerSettingsResult()
        data class Found(val settings: RunnerAndConfigurationSettings) : RunnerSettingsResult()
    }
}
