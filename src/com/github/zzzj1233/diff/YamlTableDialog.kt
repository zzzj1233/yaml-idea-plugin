package com.github.zzzj1233.diff

import com.github.zzzj1233.model.YamlDiffHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.layout.panel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

class YamlTableDialog(val project: Project, private val holderList: List<YamlDiffHolder>) : DialogWrapper(project, true) {

    private lateinit var table: JBTable

    private val WARNING = Color(230, 162, 60)

    private val tableModel = object : DefaultTableModel(createColumns(), 0) {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return false
        }
    }

    init {
        title = "Yaml check reconfirm"
        setOKButtonText("Commit")
        setCancelButtonText("Cancel")
        init()
        updateTable()
    }

    private fun updateTable() {
        holderList
                .map { it.moduleName }
                .map { arrayOf(it) }
                .forEach { this.tableModel.addRow(it) }

        // 只允许单选
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        // 监听双击事件
        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                val rowIndex: Int = (event.source as JTable).rowAtPoint(event.point)

                // 显示diffViewPanel
                showDiffDialog(rowIndex)
                return true
            }
        }.installOn(table)

        table.columnModel.getColumn(0).cellRenderer = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
                foreground = if (holderList[row].warning) WARNING else null
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            }
        }
    }

    private fun createColumns(): Array<String> {
        return arrayOf("module")
    }

    override fun createCenterPanel(): JComponent? {
        return panel {
            row {
                label("Please reconfirm yaml config")
            }

            row {
                scrollPane(JBTable(tableModel).also {
                    table = it
                    it.rowHeight = 25
                    it.preferredScrollableViewportSize = JBUI.size(600, 300)
                }).constraints(growX, growY, pushX, pushY)

                cell(isVerticalFlow = true) {
                    JButton("view").also {
                        it.addActionListener { println("table.selectedRow = ${table.selectedRow}") }
                    }(growX)
                }
            }

        }
    }

    private fun showDiffDialog(row: Int) {
        YamlDiffDialog(project, holderList[row]).show()
    }

}