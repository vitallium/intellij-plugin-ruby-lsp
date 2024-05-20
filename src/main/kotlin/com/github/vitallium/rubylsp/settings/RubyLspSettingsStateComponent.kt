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
class RubyLspSettingsStateComponent : PersistentStateComponent<RubyLspSettingsStateComponent> {
    var lspSettings: RubyLspSettings = RubyLspSettings()

    override fun getState(): RubyLspSettingsStateComponent {
        return this
    }

    override fun loadState(state: RubyLspSettingsStateComponent) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project): RubyLspSettingsStateComponent {
            return project.getService(RubyLspSettingsStateComponent::class.java)
        }
    }
}
