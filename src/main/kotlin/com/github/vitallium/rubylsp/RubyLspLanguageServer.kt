package com.github.vitallium.rubylsp

import com.github.vitallium.rubylsp.settings.RubyLspSettings
import com.github.vitallium.rubylsp.settings.RubyLspSettingsStateComponent
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.server.CannotStartProcessException
import com.redhat.devtools.lsp4ij.server.OSProcessStreamConnectionProvider
import org.jetbrains.plugins.ruby.gem.RubyGemExecutionContext
import org.jetbrains.plugins.ruby.gem.bundler.BundlerGemInfrastructure
import org.jetbrains.plugins.ruby.gem.util.BundlerUtil
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil

@Suppress("UnstableApiUsage")
class RubyLspLanguageServer(project: Project) : OSProcessStreamConnectionProvider() {
    private val logger = Logger.getInstance(RubyLspLanguageServer::class.java)

    init {
        logger.info("Creating RubyLspLanguageServer")
        val settings = RubyLspSettingsStateComponent.getInstance(project).lspSettings
        val gemExecutionContext = createGemExecutionContext(project, settings)
            ?: throw CannotStartProcessException("Ruby LSP gem is not available for this project")

        runCatching {
            val commands = gemExecutionContext.scriptExecutionCommands
                ?: throw CannotStartProcessException("Failed to build Ruby LSP command line")
            val commandLine = GeneralCommandLine(*commands)
                .withEnvironment(gemExecutionContext.additionalEnvs)

            gemExecutionContext.workingDirPath?.let(commandLine::withWorkDirectory)

            setCommandLine(commandLine)
        }.onFailure { exception ->
            when (exception) {
                is ExecutionException -> {
                    logger.error("Failed to build Ruby LSP command line", exception)
                    throw CannotStartProcessException(exception)
                }
                else -> throw exception
            }
        }
    }

    companion object {
        internal const val GEM_SCRIPT_NAME = "ruby-lsp"

        internal fun createGemExecutionContext(
            project: Project,
            lspSettings: RubyLspSettings
        ): RubyGemExecutionContext? {
            return ReadAction.compute<RubyGemExecutionContext?, RuntimeException> {
                val moduleWithGemfile = ModuleManager.getInstance(project).modules.firstNotNullOfOrNull { module ->
                    BundlerUtil.getGemfile(module)?.let { gemfile -> module to gemfile }
                } ?: return@compute null

                val (module, gemfile) = moduleWithGemfile

                if (BundlerGemInfrastructure.hasMissingGems(gemfile)) {
                    return@compute null
                }

                val sdk = RubySdkUtil.findRubySdk(project) ?: return@compute null

                RubyGemExecutionContext.tryCreate(
                    sdk,
                    module,
                    GEM_SCRIPT_NAME
                )
                    ?.withWorkingDirPath(gemfile.parent.path)
                    ?.withAddBundleExec(lspSettings.useBundler)
                    ?.withGemScriptName(GEM_SCRIPT_NAME)
            }
        }
    }
}
