package com.github.vitallium.rubylsp.settings

import com.github.vitallium.rubylsp.RubyLspBundle
import com.github.vitallium.rubylsp.services.RubyLspServerService
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class RubyLspSettingsConfigurable(private val project: Project) : Configurable, SearchableConfigurable {
    private var settingsComponent: RubyLspSettingsComponent? = null

    override fun getDisplayName(): String = RubyLspBundle.message("configurable.name.rubyLsp.settings")

    override fun getId(): String = "rubyLsp.settings"

    override fun createComponent(): JComponent? {
        val settings = RubyLspSettingsStateComponent.getInstance(project).lspSettings

        settingsComponent = RubyLspSettingsComponent(settings)
        return settingsComponent?.getPanel()
    }

    override fun isModified(): Boolean {
        return settingsComponent?.getPanel()?.isModified() ?: false
    }

    override fun apply() {
        settingsComponent?.getPanel()?.apply()

        project.service<RubyLspServerService>().restartServer()
    }

    override fun reset() {
        settingsComponent?.getPanel()?.reset()
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}
