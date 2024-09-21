package com.github.vitallium.rubylsp.settings

import com.github.vitallium.rubylsp.RubyLspBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.ListTableModel
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer

internal class RubyLspFeaturesTablePanel(enabledFeatures: List<String?>) {
    val component: JComponent

    private val model: ListTableModel<String> = ListTableModel(arrayOf(RubyLspFeatureColumnInfo()), enabledFeatures)
    private val table: TableView<String> = TableView(model).apply {
        visibleRowCount = 5
        rowSelectionAllowed = false
        tableHeader.resizingAllowed = true
        tableHeader.setUI(null)
    }

    init {
        val toolbarTable = ToolbarDecorator.createDecorator(table)
            .setAddAction { addData() }
            .setRemoveAction { removeData() }
            .addExtraAction(object : AnAction(
                RubyLspBundle.message("settings.enabledFeatures.table.action.reset"),
                RubyLspBundle.message("settings.enabledFeatures.table.action.reset.description"),
                AllIcons.General.Reset
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    onReset(RUBY_LSP_DEFAULT_FEATURES.toMutableSet())
                }
            })
            .disableUpDownActions()
            .createPanel()

        component = toolbarTable
    }

    fun onModified(features: Set<String>): Boolean {
        return model.items.toSet() != features
    }

    fun onApply(features: MutableSet<String>) {
        features.clear()
        features.addAll(model.items)
    }

    fun onReset(features: Set<String>) {
        repeat(model.items.size) { model.removeRow(0) }
        model.addRows(features)
    }

    private fun addData() {
        val newFeatureDialog = NewFeatureDialogWrapper(this.model.items)
        if (newFeatureDialog.showAndGet()) {
            val featureName = newFeatureDialog.inputData
            model.addRow(featureName)
        }
    }

    private fun removeData() {
        model.removeRow(table.selectedRow)
    }
}

internal class RubyLspFeatureColumnInfo : ColumnInfo<String, String>(null) {
    private val renderer = DefaultTableCellRenderer()

    override fun valueOf(item: String): String = item

    override fun getRenderer(item: String?): TableCellRenderer = renderer

    override fun getComparator(): java.util.Comparator<String> = COMPARATOR

    companion object {
        private val COMPARATOR = Comparator<String> { featureA, featureB ->
            featureA.compareTo(featureB, true)
        }
    }
}

internal class NewFeatureDialogWrapper(
    private val enabledFeatures: List<String?>
) : DialogWrapper(true) {
    private val featureName = JBTextField("")
    val inputData: String
        get() = featureName.text

    init {
        init()
        title = RubyLspBundle.message("settings.enabledFeatures.table.add.dialog.title")
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        val label = RubyLspBundle.message("settings.enabledFeatures.table.add.dialog.label")
        panel.add(FormBuilder().addLabeledComponent(label, featureName, 1, true).panel)
        return panel
    }

    override fun doValidate(): ValidationInfo? {
        if (!RUBY_LSP_DEFAULT_FEATURES.contains(inputData)) {
            return ValidationInfo(
                RubyLspBundle.message("settings.enabledFeatures.table.add.dialog.featureName.unknown"),
                featureName
            )
        }

        if (enabledFeatures.contains(inputData)) {
            return ValidationInfo(
                RubyLspBundle.message("settings.enabledFeatures.table.add.dialog.featureName.exists"),
                featureName
            )
        }

        return null
    }
}
