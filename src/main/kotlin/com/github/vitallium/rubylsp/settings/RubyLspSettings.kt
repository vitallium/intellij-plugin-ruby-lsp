package com.github.vitallium.rubylsp.settings

import com.google.gson.annotations.SerializedName

val rubyLspDefaultFeatures = setOf(
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
"workspaceSymbol",
)

data class RubyLspSettings(
    @Transient
    var enabled: Boolean = false,

    @Transient
    var enableExclusiveFormatting: Boolean = false,

    @Transient
    var useExperimentalVersion: Boolean = false,

    var formatter: RubyLspSettingsFormatter = RubyLspSettingsFormatter.AUTO,
    var experimentalFeaturesEnabled: Boolean = false,
    var enabledFeatures: MutableSet<String> = rubyLspDefaultFeatures.toMutableSet()
)

enum class RubyLspSettingsFormatter {
    @SerializedName("auto")
    AUTO,

    @SerializedName("rubocop")
    RUBOCOP,

    @SerializedName("syntax_tree")
    SYNTAX_TREE,

    @SerializedName("none")
    NONE;
}
