package com.github.vitallium.rubylsp.settings

import com.github.vitallium.rubylsp.RubyLspBundle
import com.github.vitallium.rubylsp.services.RubyLspServerService
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selected

class RubyLspSettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
    RubyLspBundle.message("configurable.name.rubyLsp.settings"),
    "rubyLsp"
) {
    private val rubyLspSettings = RubyLspSettingsStateComponent.getInstance(project).state

    override fun createPanel(): DialogPanel {
        return panel {
            lateinit var enableRubyLspCheckBox: Cell<JBCheckBox>
            lateinit var formatterAvailableViaAddonLabel: JBLabel

            row {
                enableRubyLspCheckBox = checkBox("Enable Ruby LSP")
                    .bindSelected(rubyLspSettings::enabled)
            }

            indent {
                row {
                    checkBox("Use pre-release versions of the Ruby LSP")
                        .enabledIf(enableRubyLspCheckBox.selected)
                        .bindSelected(rubyLspSettings::useExperimentalVersion)
                }

                row("Formatter") {
                    comboBox(RubyLspSettingsFormatter.entries)
                        .bindItem(rubyLspSettings::formatter.toNullableProperty())
                        .applyToComponent {
                            addActionListener { event ->
                                val selectedFormatter =
                                    (event.source as ComboBox<*>).selectedItem as RubyLspSettingsFormatter
                                formatterAvailableViaAddonLabel.isVisible =
                                    isStandardOrRubocopFormatterSelected(selectedFormatter)
                            }
                        }
                }

                row {
                    formatterAvailableViaAddonLabel = JBLabel(AllIcons.General.Warning)
                    cell(formatterAvailableViaAddonLabel)
                        .applyToComponent {
                            isVisible = false
                            text = "Formatter is available via addon"
                        }
                }

                row {
                    checkBox("Use bundler")
                        .enabledIf(enableRubyLspCheckBox.selected)
                        .bindSelected(rubyLspSettings::useBundler)
                }

                group("Enabled Features") {
                    row {
                        val enabledFeaturesTablePanel =
                            RubyLspFeaturesTablePanel(rubyLspSettings.enabledFeatures.toList())

                        cell(enabledFeaturesTablePanel.component)
                            .comment("List of enabled features")
                            .align(AlignX.FILL)
                            .label("Features:", LabelPosition.TOP)
                            .onIsModified { enabledFeaturesTablePanel.onModified(rubyLspSettings.enabledFeatures) }
                            .onApply { enabledFeaturesTablePanel.onApply(rubyLspSettings.enabledFeatures) }
                    }

                    row {
                        checkBox("Enable experimental features")
                            .bindSelected(rubyLspSettings::experimentalFeaturesEnabled)
                    }

                }

                group("Enabled Code Actions") {
                    row {
                        val enabledCodeActionsTablePanel =
                            RubyLspCodeActionsTablePanel(rubyLspSettings.enabledCodeActions.toList())

                        cell(enabledCodeActionsTablePanel.component)
                            .comment("List of enabled code actions")
                            .align(AlignX.FILL)
                            .label("Code actions:", LabelPosition.TOP)
                            .onIsModified { enabledCodeActionsTablePanel.onModified(rubyLspSettings.enabledCodeActions) }
                            .onApply { enabledCodeActionsTablePanel.onApply(rubyLspSettings.enabledCodeActions) }
                            .onReset { enabledCodeActionsTablePanel.onReset(RUBY_LSP_DEFAULT_CODE_ACTIONS.toMutableSet()) }
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
            }.enabledIf(enableRubyLspCheckBox.component.selected)
        }.apply { project.service<RubyLspServerService>().restartServer() }
    }

    private fun isStandardOrRubocopFormatterSelected(selectedFormatter: RubyLspSettingsFormatter): Boolean {
        return selectedFormatter == RubyLspSettingsFormatter.STANDARD ||
            selectedFormatter == RubyLspSettingsFormatter.RUBYFMT
    }
}
