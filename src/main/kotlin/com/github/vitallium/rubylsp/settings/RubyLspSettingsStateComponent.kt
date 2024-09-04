package com.github.vitallium.rubylsp.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.PROJECT)
@State(
    name = "RubyLspState",
    storages = [Storage("ruby-lsp.xml")]
)
class RubyLspSettingsStateComponent : PersistentStateComponent<RubyLspSettings> {
    var lspSettings: RubyLspSettings = RubyLspSettings()

    override fun getState(): RubyLspSettings {
        return lspSettings
    }

    override fun loadState(state: RubyLspSettings) {
        XmlSerializerUtil.copyBean(state, this.lspSettings)
    }

    companion object {
        fun getInstance(project: Project): RubyLspSettingsStateComponent {
            return project.getService(RubyLspSettingsStateComponent::class.java)
        }
    }
}
