package org.jetbrains.sampler.pw


import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridConstraints.*
import com.intellij.uiDesigner.core.GridLayoutManager
import com.test.SampleDescription
import java.awt.Dimension
import java.awt.Insets
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.JTextPane

class SamplerTemplateList(items: List<SampleDescription>) {
    val mainPanel: JPanel = JPanel()
    private val sampleDescriptions: JBList<SampleDescription> = JBList()
    private val samplePanel: JPanel = JPanel()
    private val descriptionPane: JTextPane = JTextPane()

    val selectedTemplate: SampleDescription?
        get() = sampleDescriptions.selectedValue

    init {
        sampleDescriptions.model = JBList.createDefaultListModel(items)
        setupUI()

        samplePanel.add(
                ScrollPaneFactory.createScrollPane(sampleDescriptions),
                GridConstraints(0, 0, 1, 1, ANCHOR_CENTER, FILL_BOTH, SIZEPOLICY_WANT_GROW, SIZEPOLICY_FIXED, null, null, null, 0, false)
        )

        sampleDescriptions.addListSelectionListener {
            val template = selectedTemplate
            if (template != null) {
                descriptionPane.text = template.readme
                descriptionPane.caretPosition = 0
            }
        }
        sampleDescriptions.selectedIndex = 0
    }

    private fun setupUI() {
        val scrollPane1 = JBScrollPane()
        mainPanel.apply {
            layout = GridLayoutManager(3, 3, Insets(0, 0, 0, 0), -1, -1)
            minimumSize = Dimension(400, 200)
            val separator1 = JSeparator()
            add(separator1, GridConstraints(0, 2, 1, 1, ANCHOR_CENTER, FILL_BOTH, SIZEPOLICY_WANT_GROW, SIZEPOLICY_FIXED, null, null, null, 0, false))
            add(JBLabel("Description"), GridConstraints(0, 1, 1, 1, ANCHOR_WEST, FILL_NONE, SIZEPOLICY_FIXED, SIZEPOLICY_FIXED, null, null, null, 0, false))
            add(scrollPane1, GridConstraints(1, 1, 2, 2, ANCHOR_CENTER, FILL_BOTH, SIZEPOLICY_CAN_SHRINK or SIZEPOLICY_CAN_GROW, SIZEPOLICY_CAN_SHRINK or SIZEPOLICY_CAN_GROW, null, null, null, 0, false))
        }
        descriptionPane.apply {
            //            contentType = "text/html"
            minimumSize = Dimension(200, 200)
            preferredSize = Dimension(200, 250)
            isRequestFocusEnabled = true
            autoscrolls = false
        }
        scrollPane1.setViewportView(descriptionPane)
        samplePanel.layout = GridLayoutManager(1, 1, Insets(0, 0, 0, 0), -1, -1)
        mainPanel.add(samplePanel, GridConstraints(0, 0, 3, 1, ANCHOR_CENTER, FILL_BOTH, SIZEPOLICY_CAN_SHRINK or SIZEPOLICY_CAN_GROW, SIZEPOLICY_CAN_SHRINK or SIZEPOLICY_CAN_GROW, Dimension(150, 200), Dimension(-1, 250), null, 1, false))
    }
}
