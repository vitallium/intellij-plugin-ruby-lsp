package com.github.vitallium.rubylsp.settings

import com.github.vitallium.rubylsp.RubyLspBundle
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


internal class RubyLspCodeActionsTablePanel {
    val component: JComponent

    private val model: ListTableModel<String> = ListTableModel(RubyLspCodeActionColumnInfo())
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

    fun onModified(codeActions: MutableSet<String>): Boolean {
        return model.items != codeActions
    }

    fun onApply(codeActions: MutableSet<String>) {
        codeActions.clear()
        model.items.forEach {
            codeActions.add(it)
        }
    }

    fun onReset(codeActions: MutableSet<String>) {
        repeat(model.items.size) { model.removeRow(0) }
        model.addRows(codeActions)
    }

    private fun addData() {
        val newCodeActionDialog = NewCodeActionDialogWrapper(this.model.items)
        if (newCodeActionDialog.showAndGet()) {
            val codeActionName = newCodeActionDialog.inputData
            model.addRow(codeActionName)
        }
    }

    private fun removeData() {
        model.removeRow(table.selectedRow)
    }
}

internal class RubyLspCodeActionColumnInfo : ColumnInfo<String, String>(null) {
    private val renderer = DefaultTableCellRenderer()

    override fun valueOf(item: String): String = item

    override fun getRenderer(item: String?): TableCellRenderer = renderer

    override fun getComparator(): java.util.Comparator<String> = COMPARATOR

    companion object {
        private val COMPARATOR = Comparator<String> { codeActionA, codeActionB ->
            codeActionA.compareTo(codeActionB, true)
        }
    }
}

internal class NewCodeActionDialogWrapper(
    private val enabledCodeActions: List<String?>
) : DialogWrapper(true) {
    private val codeActionName = JBTextField("")
    val inputData: String
        get() = codeActionName.text

    init {
        init()
        title = RubyLspBundle.message("settings.enabledCodeActions.table.add.dialog.title")
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        val label = RubyLspBundle.message("settings.enabledCodeActions.table.add.dialog.label")
        panel.add(FormBuilder().addLabeledComponent(label, codeActionName, 1, true).panel)
        return panel
    }

    override fun doValidate(): ValidationInfo? {
        if (!rubyLspDefaultCodeActions.contains(inputData)) {
            return ValidationInfo(
                RubyLspBundle.message("settings.enabledCodeActions.table.add.dialog.codeActionName.unknown"),
                codeActionName
            )
        }

        if (enabledCodeActions.contains(inputData)) {
            return ValidationInfo(
                RubyLspBundle.message("settings.enabledCodeActions.table.add.dialog.codeActionName.exists"),
                codeActionName
            )
        }

        return null
    }
}
