package com.github.vitallium.rubylsp

import com.github.vitallium.rubylsp.settings.RubyLspSettings
import com.github.vitallium.rubylsp.settings.RubyLspSettingsStateComponent
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.redhat.devtools.lsp4ij.LanguageServerEnablementSupport
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.client.features.*
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider
import org.eclipse.lsp4j.InitializeParams
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType

class RubyLspLanguageServerFactory : LanguageServerFactory, LanguageServerEnablementSupport {
    override fun createConnectionProvider(project: Project): StreamConnectionProvider {
        return RubyLspLanguageServer(project)
    }

    @Suppress("UnstableApiUsage")
    override fun createClientFeatures(): LSPClientFeatures = RubyLspClientFeatures()

    override fun isEnabled(project: Project): Boolean {
        val settings = RubyLspSettingsStateComponent.getInstance(project)

        return settings.lspSettings.enabled
    }

    override fun setEnabled(enabled: Boolean, project: Project) {
        val settings = RubyLspSettingsStateComponent.getInstance(project)
        settings.lspSettings.enabled = enabled
    }
}

@Suppress("UnstableApiUsage")
private class RubyLspClientFeatures : LSPClientFeatures() {
    private val gson = Gson()

    init {
        setServerInstaller(RubyLspLanguageServerInstaller())
        diagnosticFeature = object : LSPDiagnosticFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("diagnostics", file) && super.isEnabled(file)
            }
        }
        codeActionFeature = object : LSPCodeActionFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("codeActions", file) && super.isEnabled(file)
            }

            override fun isQuickFixesEnabled(file: PsiFile): Boolean {
                return featureEnabled("codeActions", file) &&
                    lspSettings(file.project).enabledCodeActions.contains("quickfix") &&
                    super.isQuickFixesEnabled(file)
            }

            override fun isIntentionActionsEnabled(file: PsiFile): Boolean {
                return featureEnabled("codeActions", file) && super.isIntentionActionsEnabled(file)
            }
        }
        completionFeature = object : LSPCompletionFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("completion", file) && super.isEnabled(file)
            }
        }
        definitionFeature = object : LSPDefinitionFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("definition", file) && super.isEnabled(file)
            }
        }
        hoverFeature = object : LSPHoverFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("hover", file) && super.isEnabled(file)
            }
        }
        documentSymbolFeature = object : LSPDocumentSymbolFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("documentSymbols", file) && super.isEnabled(file)
            }
        }
        documentLinkFeature = object : LSPDocumentLinkFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("documentLink", file) && super.isEnabled(file)
            }
        }
        documentHighlightFeature = object : LSPDocumentHighlightFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("documentHighlights", file) && super.isEnabled(file)
            }
        }
        inlayHintFeature = object : LSPInlayHintFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("inlayHint", file) && super.isEnabled(file)
            }
        }
        semanticTokensFeature = object : LSPSemanticTokensFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("semanticHighlighting", file) && super.isEnabled(file)
            }
        }
        signatureHelpFeature = object : LSPSignatureHelpFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("signatureHelp", file) && super.isEnabled(file)
            }
        }
        workspaceSymbolFeature = object : LSPWorkspaceSymbolFeature() {
            override fun isEnabled(): Boolean {
                return lspSettings(project).enabledFeatures.contains("workspaceSymbol") && super.isEnabled()
            }
        }
        codeLensFeature = object : LSPCodeLensFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("codeLens", file) && super.isEnabled(file)
            }
        }
        foldingRangeFeature = object : LSPFoldingRangeFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("foldingRanges", file) && super.isEnabled(file)
            }
        }
        selectionRangeFeature = object : LSPSelectionRangeFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("selectionRanges", file) && super.isEnabled(file)
            }
        }
        typeHierarchyFeature = object : LSPTypeHierarchyFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("typeHierarchy", file) && super.isEnabled(file)
            }
        }
        formattingFeature = object : LSPFormattingFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("formatting", file) && super.isEnabled(file)
            }

            override fun isExistingFormatterOverrideable(file: PsiFile): Boolean {
                return lspSettings(file.project).enableExclusiveFormatting
            }
        }
        onTypeFormattingFeature = object : LSPOnTypeFormattingFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("onTypeFormatting", file) && super.isEnabled(file)
            }
        }
        declarationFeature = object : LSPDeclarationFeature() {
            override fun isEnabled(file: PsiFile): Boolean {
                return featureEnabled("definition", file) && super.isEnabled(file)
            }
        }
    }

    override fun initializeParams(params: InitializeParams) {
        super.initializeParams(params)
        params.initializationOptions = initializationOptions()
    }

    private fun initializationOptions(): JsonElement? {
        val settings = lspSettings(project)
        val initOptions = gson.toJsonTree(settings)

        if (settings.experimentalFeaturesEnabled) {
            val experimentalOptions = gson.toJsonTree(mapOf("addon_detection" to true))
            initOptions.asJsonObject.add("experimental", experimentalOptions)
        }

        return initOptions
    }

    private fun featureEnabled(featureName: String, file: PsiFile): Boolean {
        return isRubyFile(file) && lspSettings(file.project).enabledFeatures.contains(featureName)
    }

    private fun isRubyFile(file: PsiFile): Boolean {
        return file.fileType == RubyFileType.RUBY
    }

    private fun lspSettings(project: Project): RubyLspSettings {
        return RubyLspSettingsStateComponent.getInstance(project).lspSettings
    }
}
