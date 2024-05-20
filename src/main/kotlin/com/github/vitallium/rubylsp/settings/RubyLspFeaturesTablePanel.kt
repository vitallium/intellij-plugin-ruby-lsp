package com.github.vitallium.rubylsp.settings

import com.github.vitallium.rubylsp.RubyLspBundle
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.MessageDialogBuilder
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


internal class RubyLspFeaturesTablePanel {
    val component: JComponent

    private val model: ListTableModel<String> = ListTableModel(RubyLspFeatureColumnInfo())
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
            .disableUpDownActions()
            .createPanel()

        component = toolbarTable
    }

    fun onModified(features: MutableSet<String>): Boolean {
        return model.items != features
    }

    fun onApply(features: MutableSet<String>) {
        features.clear()
        model.items.forEach {
            features.add(it)
        }
    }

    fun onReset(features: MutableSet<String>) {
        repeat(model.items.size) { model.removeRow(0) }
        model.addRows(features)
    }

    private fun addData() {
        val newFeatureDialog = NewFeatureDialogWrapper()
        if (newFeatureDialog.showAndGet()) {
            val featureName = newFeatureDialog.inputData
            model.addRow(featureName)
        }
    }

    private fun removeData() {
        val dialog = MessageDialogBuilder.okCancel(
            RubyLspBundle.message("settings.enabledFeatures.table.remove.dialog.title"),
            RubyLspBundle.message("settings.enabledFeatures.table.remove.dialog.message")
        )

        if (dialog.guessWindowAndAsk()) {
            model.removeRow(table.selectedRow)
        }
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


internal class NewFeatureDialogWrapper : DialogWrapper(true) {
    private val input = JBTextField("")
    val inputData: String
        get() = input.text

    init {
        init()
        title = RubyLspBundle.message("settings.enabledFeatures.table.add.dialog.title")
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        val label = RubyLspBundle.message("settings.enabledFeatures.table.add.dialog.label")
        panel.add(FormBuilder().addLabeledComponent(label, input, 1, true).panel)
        return panel
    }
}
