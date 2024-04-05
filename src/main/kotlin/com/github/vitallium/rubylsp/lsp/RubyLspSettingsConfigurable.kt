package com.github.vitallium.rubylsp.lsp

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected

@Suppress("UnstableApiUsage")
class RubyLspSettingsConfigurable(private val project: Project) : BoundConfigurable("Ruby LSP Server") {
    override fun createPanel(): DialogPanel {
        val rubyLspConfigProperties = RubyLspConfigProperties(PropertiesComponent.getInstance(project))

        lateinit var pluginEnabledCheckbox: Cell<JBCheckBox>

        return panel {
            row {
                pluginEnabledCheckbox = checkBox("Enable Ruby LSP").bindSelected(rubyLspConfigProperties::pluginEnabled)
            }
            row {
                checkBox("Enable experiments")
                    .bindSelected(rubyLspConfigProperties::experimentsEnabled)
                    .enabledIf(pluginEnabledCheckbox.selected)
                    .comment("Disabled by default.")
            }
            row {
                checkBox("Enable exclusive formatting")
                    .bindSelected(rubyLspConfigProperties::formattingEnabled)
                    .enabledIf(pluginEnabledCheckbox.selected)
                    .comment("Replaces the RubyMine formatting.")
            }

            onApply {
                LspServerManager.getInstance(project).stopAndRestartIfNeeded(RubyLspServerSupportProvider::class.java)
            }
        }
    }
}
