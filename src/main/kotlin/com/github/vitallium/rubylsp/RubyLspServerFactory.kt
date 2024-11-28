package com.github.vitallium.rubylsp

import com.github.vitallium.rubylsp.settings.RubyLspSettingsStateComponent
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.LanguageServerEnablementSupport
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider

class RubyLspServerFactory  : LanguageServerFactory, LanguageServerEnablementSupport {
    override fun createConnectionProvider(project: Project): StreamConnectionProvider {
        return RubyLspLanguageServer.create(project)
    }

    override fun isEnabled(project: Project): Boolean {
        if (!validateLSP4IJCompatibility()) {
            return false
        }

        val settings = RubyLspSettingsStateComponent.getInstance(project)

        return settings.lspSettings.enabled
    }

    private fun validateLSP4IJCompatibility(): Boolean {
        try {
            Class.forName("com.redhat.devtools.lsp4ij.server.OSProcessStreamConnectionProvider")
            return true
        } catch (_: ClassNotFoundException) {
            return false
        }
    }

    override fun setEnabled(enabled: Boolean, project: Project) {
        val settings = RubyLspSettingsStateComponent.getInstance(project)
        settings.lspSettings.enabled = enabled
    }
}
