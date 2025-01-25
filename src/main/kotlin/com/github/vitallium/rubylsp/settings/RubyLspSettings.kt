package com.github.vitallium.rubylsp.settings

import com.google.gson.annotations.SerializedName

// For the list of available features, see https://github.com/Shopify/ruby-lsp/blob/main/lib/ruby_lsp/server.rb
val RUBY_LSP_DEFAULT_FEATURES = setOf(
    "codeActions",
    "codeLens",
    "completion",
    "definition",
    "diagnostics",
    "documentHighlights",
    "documentLink",
    "documentSymbols",
    "foldingRanges",
    "formatting",
    "hover",
    "inlayHint",
    "onTypeFormatting",
    "selectionRanges",
    "semanticHighlighting",
    "signatureHelp",
    "typeHierarchy",
    "workspaceSymbol",
)

// For the list of available code actions, see https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#codeActionKind
val RUBY_LSP_DEFAULT_CODE_ACTIONS = setOf(
    "quickfix",
    "refactor.extract",
    "refactor.inline",
    "refactor.rewrite",
    "source.organizeImports",
    "source.fixAll"
)

data class RubyLspSettings(
    @Transient
    var enabled: Boolean = false,

    @Transient
    var enableExclusiveFormatting: Boolean = false,

    @Transient
    var useBundler: Boolean = false,

    var formatter: RubyLspSettingsFormatter = RubyLspSettingsFormatter.AUTO,
    var experimentalFeaturesEnabled: Boolean = false,
    var enabledFeatures: MutableSet<String> = RUBY_LSP_DEFAULT_FEATURES.toMutableSet(),
    var enabledCodeActions: MutableSet<String> = RUBY_LSP_DEFAULT_CODE_ACTIONS.toMutableSet()
)

enum class RubyLspSettingsFormatter {
    @SerializedName("auto")
    @DisplayName("Automatic")
    AUTO,

    @SerializedName("rubocop")
    @DisplayName("RuboCop")
    RUBOCOP,

    @SerializedName("syntax_tree")
    @DisplayName("Syntax Tree")
    SYNTAX_TREE,

    @SerializedName("standard")
    @DisplayName("Standard")
    STANDARD,

    @SerializedName("rubyfmt")
    @DisplayName("Rubyfmt")
    RUBYFMT,

    @SerializedName("none")
    @DisplayName("None")
    NONE;

    fun getDisplayName(): String {
        return this.javaClass.getField(this.name).getAnnotation(DisplayName::class.java)?.name ?: this.name
    }
}
