package com.github.vitallium.rubylsp.lsp

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.execution.process.OSProcessHandler
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspCommunicationChannel
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.customization.LspCompletionSupport
import com.intellij.platform.lsp.api.customization.LspFormattingSupport
import org.eclipse.lsp4j.CompletionItem
import org.jetbrains.plugins.ruby.gem.RubyGemExecutionContext
import org.jetbrains.plugins.ruby.gem.bundler.BundlerGemInfrastructure
import org.jetbrains.plugins.ruby.gem.util.BundlerUtil
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType

@Suppress("UnstableApiUsage")
internal class RubyLspServerDescriptor(
    private val rubyLspConfigProperties: RubyLspConfigProperties,
    private val rubyExecutionContext: RubyGemExecutionContext,
    project: Project,
    roots: Array<out VirtualFile>
) : ProjectWideLspServerDescriptor(project, "Ruby LSP") {
    override fun isSupportedFile(file: VirtualFile): Boolean {
        return file.fileType == RubyFileType.RUBY
    }

    override val lspCommunicationChannel: LspCommunicationChannel
        get() = LspCommunicationChannel.StdIO

    override val lspFormattingSupport = object : LspFormattingSupport() {
        override fun shouldFormatThisFileExclusivelyByServer(file: VirtualFile, ideCanFormatThisFileItself: Boolean, serverExplicitlyWantsToFormatThisFile: Boolean): Boolean {
            return rubyLspConfigProperties.formattingEnabled;
        }
    }

    override val lspCompletionSupport: LspCompletionSupport
        get() = object : LspCompletionSupport() {
            override fun createLookupElement(parameters: CompletionParameters, item: CompletionItem): LookupElement? {
                val item = super.createLookupElement(parameters, item) ?: return null
                // we want to be more preferable than TextMate
                return PrioritizedLookupElement.withPriority(item, 1.0)
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

            return RubyGemExecutionContext.tryCreate(null, module, GEM_SCRIPT_NAME)
                ?.withWorkingDir(gemfile.parent)
                ?.withGemScriptName(GEM_SCRIPT_NAME)
                ?.withArguments(gemScriptArgsBuilder)
        }

        fun tryCreate(project: Project, file: VirtualFile): RubyLspServerDescriptor? {
            val rubyLspConfigProperties = RubyLspConfigProperties(PropertiesComponent.getInstance(project))
            if (!rubyLspConfigProperties.pluginEnabled) return null

            val executionContext = createGemExecutionContext(project, file) ?: return null
            return RubyLspServerDescriptor(rubyLspConfigProperties, executionContext, project, executionContext.module!!.rootManager.contentRoots)
        }
    }
}
