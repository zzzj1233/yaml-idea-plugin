package com.github.zzzj1233.diff

import com.github.zzzj1233.enums.GhEnv
import com.github.zzzj1233.model.YamlDiffContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.layout.panel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.table.DefaultTableModel

class YamlTableDialog(val project: Project, private val yamlDiffContext: YamlDiffContext,
                      dialogTitle: String = "Yaml check reconfirm",
                      okText: String = "Commit",
                      cancelText: String = "Cancel",
                      private val label: String = "Please reconfirm yaml config",
                      private val leftPanelTitle: String = "BeforeCommit",
                      private val rightPanelTitle: String = "AfterCommit"
) : DialogWrapper(project, true) {

    private lateinit var table: JBTable

    private val WARNING = Color(230, 162, 60)

    private var selectedEnv: GhEnv? = GhEnv.PROD

    private var modules: List<String> = emptyList()

    private var tableModel = DefaultTableModel(createColumns(), 0)

    var commit = false

    init {
        title = dialogTitle
        setOKButtonText(okText)
        setCancelButtonText(cancelText)
        init()
        updateModel()

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
    }

    private fun updateModel() {
        tableModel = object : DefaultTableModel(createColumns(), 0) {
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return false
            }
        }
        modules = yamlDiffContext[selectedEnv!!].keys.toList()
        modules.forEach { tableModel.addRow(arrayOf(it)) }
        table.model = tableModel
//        table.columnModel.getColumn(0).cellRenderer = object : DefaultTableCellRenderer() {
//            override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
//                // foreground = if (holderList[row].warning) WARNING else null
//                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
//            }
//        }
    }

    private fun createColumns(): Array<String> {
        return arrayOf("module")
    }


    override fun createCenterPanel(): JComponent? {
        return panel {
            row {
                label(label)
            }

            row {
                scrollPane(JBTable(tableModel).also {
                    table = it
                    it.rowHeight = 25
                    it.preferredScrollableViewportSize = JBUI.size(600, 300)
                }).constraints(growX, growY, pushX, pushY)

                cell(isVerticalFlow = true) {
                    JButton("View").also {
                        it.addActionListener { showDiffDialog(table.selectedRow) }
                    }(growX)
                    comboBox<GhEnv>(object : DefaultComboBoxModel<GhEnv>(GhEnv.values()) {
                        override fun setSelectedItem(anObject: Any?) {
                            super.setSelectedItem(anObject)
                            (anObject as? GhEnv)?.apply {
                                selectedEnv = this
                                updateModel()
                            }
                        }
                    }, { selectedEnv }, {
                        selectedEnv = it
                    })
                }
            }
        }
    }

    private fun showDiffDialog(row: Int) {
        if (row >= 0) {
            YamlDiffDialog(project, yamlDiffContext[selectedEnv!!][modules[row]]!!, leftPanelTitle, rightPanelTitle).show()
        }
    }

    override fun doOKAction() {
        commit = true
        super.doOKAction()
    }

    override fun doCancelAction() {
        commit = false
        super.doCancelAction()
    }

}