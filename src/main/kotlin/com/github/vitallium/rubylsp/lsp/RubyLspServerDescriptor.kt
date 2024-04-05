package com.github.vitallium.rubylsp.lsp

import com.intellij.execution.process.OSProcessHandler
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspCommunicationChannel
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.customization.LspFormattingSupport
import org.jetbrains.plugins.ruby.gem.RubyGemExecutionContext
import org.jetbrains.plugins.ruby.gem.bundler.BundlerGemInfrastructure
import org.jetbrains.plugins.ruby.gem.util.BundlerUtil
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil

@Suppress("UnstableApiUsage")
internal class RubyLspServerDescriptor(
    private val rubyLspConfigProperties: RubyLspConfigProperties,
    private val rubyExecutionContext: RubyGemExecutionContext,
    project: Project,
) : ProjectWideLspServerDescriptor(project, "Ruby LSP") {
    override fun isSupportedFile(file: VirtualFile): Boolean {
        return file.fileType == RubyFileType.RUBY
    }

    override val lspCommunicationChannel: LspCommunicationChannel
        get() = LspCommunicationChannel.StdIO

    override val lspFormattingSupport = object : LspFormattingSupport() {
        override fun shouldFormatThisFileExclusivelyByServer(file: VirtualFile, ideCanFormatThisFileItself: Boolean, serverExplicitlyWantsToFormatThisFile: Boolean): Boolean {
            return serverExplicitlyWantsToFormatThisFile && rubyLspConfigProperties.formattingEnabled
        }
    }

    override fun startServerProcess(): OSProcessHandler {
        val processHandler = rubyExecutionContext.toRubyScriptExecutionContext()!!.createProcessHandler()
        if (processHandler !is OSProcessHandler) {
            throw RuntimeException("hmm... RubyProcessHandler wasn't an OSProcessHandler.")
        }
        return processHandler
    }

    companion object {
        private const val GEM_SCRIPT_NAME = "ruby-lsp"

        private fun createGemExecutionContext(project: Project, file: VirtualFile): RubyGemExecutionContext? {
            val module = ModuleUtilCore.findModuleForFile(file, project)
            val gemfile = BundlerUtil.getGemfile(module) ?: return null
            if (BundlerGemInfrastructure.hasMissingGems(gemfile)) {
                return null
            }

            val rubyLspConfigProperties = RubyLspConfigProperties(PropertiesComponent.getInstance(project))
            val gemScriptArgsBuilder = mutableListOf("")

            if (rubyLspConfigProperties.experimentsEnabled) {
                gemScriptArgsBuilder += "--experimental"
            }

            return RubyGemExecutionContext.tryCreate(RubySdkUtil.getFileSdk(project, file), null, GEM_SCRIPT_NAME)
                ?.withWorkingDir(gemfile.parent)
                ?.withGemScriptName(GEM_SCRIPT_NAME)
                ?.withArguments(gemScriptArgsBuilder)
        }

        fun tryCreate(project: Project, file: VirtualFile): RubyLspServerDescriptor? {
            val rubyLspConfigProperties = RubyLspConfigProperties(PropertiesComponent.getInstance(project))
            if (!rubyLspConfigProperties.pluginEnabled) return null

            val executionContext = createGemExecutionContext(project, file) ?: return null
            return RubyLspServerDescriptor(rubyLspConfigProperties, executionContext, project)
        }
    }
}
