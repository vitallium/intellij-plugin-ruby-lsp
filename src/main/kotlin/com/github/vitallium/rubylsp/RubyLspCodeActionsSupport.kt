package com.github.vitallium.rubylsp

import com.github.vitallium.rubylsp.settings.RubyLspSettings
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.customization.LspCodeActionsSupport
import org.eclipse.lsp4j.CodeAction

internal class RubyLspCodeActionsSupport(rubyLspSettings: RubyLspSettings) : LspCodeActionsSupport() {
    private val enabledCodeActions = rubyLspSettings.enabledCodeActions

    override fun createIntentionAction(lspServer: LspServer, codeAction: CodeAction) = when {
        !enabledCodeActions.contains(codeAction.kind) -> null
        else -> super.createIntentionAction(lspServer, codeAction)
    }
}
