package com.github.vitallium.rubylsp.commands

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.redhat.devtools.lsp4ij.commands.LSPCommand
import com.redhat.devtools.lsp4ij.commands.LSPCommandAction
import java.nio.file.Paths

class RubyLspRunTestInTerminalCommand : LSPCommandAction() {
    private val logger = thisLogger()

    override fun commandPerformed(command: LSPCommand, e: AnActionEvent) {
        val project = e.project ?: return
        val arguments = command.originalArguments ?: command.arguments
        val parsedArgs = RubyLspTestCommandArgs.from(arguments) ?: run {
            logger.warn("Ruby LSP run test in terminal: missing or invalid command arguments")
            notifyWarning(project, "Ruby LSP: unable to run test in terminal (missing command arguments).")
            return
        }

        if (!runInTerminal(project, parsedArgs)) {
            notifyWarning(project, "Ruby LSP: no test command available for terminal execution.")
        }
    }

    private fun runInTerminal(project: Project, args: RubyLspTestCommandArgs): Boolean {
        val commandString = args.command?.trim().orEmpty()
        if (commandString.isBlank()) {
            return false
        }

        val workingDirectory = resolveWorkingDirectory(project, args.path)
        if (workingDirectory.isNullOrBlank()) {
            return false
        }

        ApplicationManager.getApplication().invokeLater {
            try {
                val terminalViewClass = Class.forName("org.jetbrains.plugins.terminal.TerminalView")
                val getInstance = terminalViewClass.getMethod("getInstance", Project::class.java)
                val terminalView = getInstance.invoke(null, project)
                val createWidget = terminalViewClass.getMethod("createLocalShellWidget", String::class.java, String::class.java)
                val title = args.name?.let { "Ruby LSP: $it" } ?: "Ruby LSP: Test Command"
                val widget = createWidget.invoke(terminalView, workingDirectory, title)
                val executeCommand = widget.javaClass.getMethod("executeCommand", String::class.java)
                executeCommand.invoke(widget, commandString)
            } catch (exception: ClassNotFoundException) {
                notifyWarning(project, "Ruby LSP: terminal plugin is not available.")
            } catch (exception: Exception) {
                logger.warn("Ruby LSP run test in terminal: failed to execute command", exception)
                notifyWarning(project, "Ruby LSP: failed to execute test command in terminal.")
            }
        }

        return true
    }

    private fun resolveWorkingDirectory(project: Project, path: String): String? {
        project.basePath?.let { return it }
        val file = resolveFile(project, path)
        return file?.parent?.path
    }

    private fun resolveFile(project: Project, path: String): com.intellij.openapi.vfs.VirtualFile? {
        val normalizedPath = if (path.startsWith("file://")) VfsUtilCore.urlToPath(path) else path
        val fileSystem = LocalFileSystem.getInstance()
        return fileSystem.findFileByPath(normalizedPath)
            ?: project.basePath?.let { base ->
                fileSystem.findFileByPath(Paths.get(base, normalizedPath).toString())
            }
    }

    private fun notifyWarning(project: Project, message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("RubyLsp")
            .createNotification(message, NotificationType.WARNING)
            .notify(project)
    }
}
