package com.github.vitallium.rubylsp

import com.github.vitallium.rubylsp.settings.RubyLspSettingsStateComponent
import com.intellij.execution.ExecutionException
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.VirtualFile
import com.redhat.devtools.lsp4ij.installation.LanguageServerInstallerBase
import org.jetbrains.plugins.ruby.gem.RubyGemExecutionContext
import org.jetbrains.plugins.ruby.gem.util.BundlerUtil
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil

@Suppress("UnstableApiUsage")
class RubyLspLanguageServerInstaller : LanguageServerInstallerBase() {
    override fun checkServerInstalled(indicator: ProgressIndicator): Boolean {
        progressCheckingServerInstalled(indicator)

        val currentProject = project ?: return false
        val settings = RubyLspSettingsStateComponent.getInstance(currentProject).lspSettings
        val executionContext = RubyLspLanguageServer.createGemExecutionContext(currentProject, settings) ?: return false

        return runCatching {
            executionContext.scriptExecutionCommands
            true
        }.getOrElse { false }
    }

    override fun install(indicator: ProgressIndicator) {
        progressInstallingServer(indicator)
        indicator.text = "Installing ruby-lsp gem"

        val currentProject = project
            ?: throw RuntimeException("Project is not available for installation")

        val settings = RubyLspSettingsStateComponent.getInstance(currentProject).lspSettings

        val moduleWithGemfile = ReadAction.compute<Pair<com.intellij.openapi.module.Module, VirtualFile>?, RuntimeException> {
            ModuleManager.getInstance(currentProject).modules.firstNotNullOfOrNull { module ->
                BundlerUtil.getGemfile(module)?.let { gemfile -> module to gemfile }
            }
        } ?: throw RuntimeException("A module with a Gemfile is required to install ruby-lsp")

        val (module, gemfile) = moduleWithGemfile

        val sdk = ReadAction.compute<Sdk?, RuntimeException> {
            RubySdkUtil.findRubySdk(currentProject)
        } ?: throw RuntimeException("Ruby SDK is not configured")

        val gemExecutionContext = RubyGemExecutionContext.create(sdk, "gem")
            .withModule(module)
            .withWorkingDirPath(gemfile.parent.path)
            .withAddBundleExec(settings.useBundler)

        val scriptExecutionContext = gemExecutionContext
            .toRubyScriptExecutionContext()
            ?.withArguments("install", RubyLspLanguageServer.GEM_SCRIPT_NAME)
            ?: throw RuntimeException("Failed to build gem installation command")

        runCatching {
            scriptExecutionContext.executeScript()
        }.onSuccess { output ->
            if (output == null) {
                throw RuntimeException("gem install ruby-lsp failed: no process output")
            }
            if (output.exitCode != 0) {
                throw RuntimeException("gem install ruby-lsp failed with exit code ${output.exitCode}")
            }
        }.onFailure { exception ->
            when (exception) {
                is ExecutionException -> throw RuntimeException("Failed to install ruby-lsp gem", exception)
                else -> throw exception
            }
        }
    }
}
