package com.github.vitallium.rubylsp.settings

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*

class RubyLspSettingsComponent(private val rubyLspSettings: RubyLspSettings) {
    private var panel: DialogPanel? = null

    init {
        panel = panel {
            lateinit var enableRubyLspCheckBox: Cell<JBCheckBox>

            group("General Settings") {
                row {
                    enableRubyLspCheckBox = checkBox("Enable Ruby LSP")
                        .bindSelected(rubyLspSettings::enabled)
                }

                row {
                    checkBox("Use pre-release versions of the Ruby LSP")
                        .enabledIf(enableRubyLspCheckBox.selected)
                        .bindSelected(rubyLspSettings::useExperimentalVersion)
                }

                row("Formatter") {
                    comboBox(RubyLspSettingsFormatter.entries)
                        .bindItem(rubyLspSettings::formatter.toNullableProperty())
                }
            }

            group("Enabled Features") {
                row {
                    val enabledFeaturesTablePanel = RubyLspFeaturesTablePanel()

                    cell(enabledFeaturesTablePanel.component)
                        .comment("List of enabled features")
                        .align(AlignX.FILL)
                        .label("Features:", LabelPosition.TOP)
                        .onIsModified { enabledFeaturesTablePanel.onModified(rubyLspSettings.enabledFeatures) }
                        .onApply { enabledFeaturesTablePanel.onApply(rubyLspSettings.enabledFeatures) }
                        .onReset { enabledFeaturesTablePanel.onReset(rubyLspDefaultFeatures.toMutableSet()) }
                }

                row {
                    checkBox("Enable experimental features")
                        .bindSelected(rubyLspSettings::experimentalFeaturesEnabled)
                }
            }

            group("Intellij Specific Settings") {
                row {
                    checkBox("Enable exclusive formatting")
                        .enabledIf(enableRubyLspCheckBox.selected)
                        .comment("Replaces the RubyMine formatting.")
                        .bindSelected(rubyLspSettings::enableExclusiveFormatting)
                }
            }
        }
    }

    fun getPanel(): DialogPanel? {
        return panel
    }
}
