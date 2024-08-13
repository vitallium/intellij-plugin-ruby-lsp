package com.github.vitallium.rubylsp

import com.github.vitallium.rubylsp.settings.RubyLspSettings
import com.github.vitallium.rubylsp.settings.RubyLspSettingsStateComponent
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.customization.LspCodeActionsSupport
import com.intellij.platform.lsp.api.customization.LspCompletionSupport
import com.intellij.platform.lsp.api.customization.LspDiagnosticsSupport
import com.intellij.platform.lsp.api.customization.LspFormattingSupport
import org.jetbrains.plugins.ruby.gem.RubyGemExecutionContext
import org.jetbrains.plugins.ruby.gem.bundler.BundlerGemInfrastructure
import org.jetbrains.plugins.ruby.gem.util.BundlerUtil
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil

@Suppress("UnstableApiUsage")
internal class RubyLspServerDescriptor(
    private val rubyLspSettings: RubyLspSettings,
    private val rubyExecutionContext: RubyGemExecutionContext,
    project: Project,
) : ProjectWideLspServerDescriptor(project, "Ruby LSP") {
    private val logger = Logger.getInstance("RubyLSP")

    private val initializationOptions: JsonElement?

    init {
        initializationOptions = prepareInitializationOptions()
    }

    override fun isSupportedFile(file: VirtualFile): Boolean {
        return file.fileType == RubyFileType.RUBY
    }

    override val lspFormattingSupport = object : LspFormattingSupport() {
        override fun shouldFormatThisFileExclusivelyByServer(
            file: VirtualFile,
            ideCanFormatThisFileItself: Boolean,
            serverExplicitlyWantsToFormatThisFile: Boolean
        ): Boolean {
            return rubyLspSettings.enableExclusiveFormatting
        }
    }

    override fun createInitializationOptions(): JsonElement? {
        logger.debug(initializationOptions.toString())
        return initializationOptions
    }

    override fun startServerProcess(): OSProcessHandler {
        val processHandler = rubyExecutionContext.toRubyScriptExecutionContext()!!.createProcessHandler()
        if (processHandler !is OSProcessHandler) {
            throw RuntimeException("hmm... RubyProcessHandler wasn't an OSProcessHandler.")
        }

        return processHandler
    }

    override val lspDiagnosticsSupport =
        LspDiagnosticsSupport().takeIf { rubyLspSettings.enabledFeatures.contains("diagnostics") }
    override val lspCodeActionsSupport =
        LspCodeActionsSupport().takeIf { rubyLspSettings.enabledFeatures.contains("codeActions") }
    override val lspCompletionSupport =
        LspCompletionSupport().takeIf { rubyLspSettings.enabledFeatures.contains("completion") }
    override val lspGoToDefinitionSupport = rubyLspSettings.enabledFeatures.contains("definition")
    override val lspHoverSupport = rubyLspSettings.enabledFeatures.contains("hover")

    private fun prepareInitializationOptions(): JsonElement? {
        return Gson().toJsonTree(rubyLspSettings)
    }

    companion object {
        private const val GEM_SCRIPT_NAME = "ruby-lsp"

        private fun createGemExecutionContext(
            project: Project,
            file: VirtualFile,
            lspSettings: RubyLspSettings
        ): RubyGemExecutionContext? {
            val module = ModuleUtilCore.findModuleForFile(file, project)
            val gemfile = BundlerUtil.getGemfile(module) ?: return null

            if (BundlerGemInfrastructure.hasMissingGems(gemfile)) {
                return null
            }

            val gemScriptArgsBuilder = mutableListOf("")

            if (lspSettings.useExperimentalVersion) {
                gemScriptArgsBuilder.add("--experimental")
            }

            val moduleForRunningLsp = if (lspSettings.useBundler) module else null

            return RubyGemExecutionContext.tryCreate(RubySdkUtil.getFileSdk(project, file), moduleForRunningLsp, GEM_SCRIPT_NAME)
                ?.withWorkingDir(gemfile.parent)
                ?.withGemScriptName(GEM_SCRIPT_NAME)
                ?.withArguments(gemScriptArgsBuilder)
        }

        fun tryCreate(project: Project, file: VirtualFile): RubyLspServerDescriptor? {
            val rubyLspSettingsState = RubyLspSettingsStateComponent.getInstance(project)

            if (!rubyLspSettingsState.lspSettings.enabled) {
                return null
            }

            val executionContext =
                createGemExecutionContext(project, file, rubyLspSettingsState.lspSettings) ?: return null
            return RubyLspServerDescriptor(rubyLspSettingsState.lspSettings, executionContext, project)
        }
    }
}
