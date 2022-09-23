package com.github.lure0xaos.jrpycg.ui.comp.about

import com.github.lure0xaos.jrpycg.res.Res
import com.github.lure0xaos.jrpycg.services.LocaleHolder
import com.github.lure0xaos.util.get
import com.github.lure0xaos.util.getResource
import com.github.lure0xaos.util.getResourceBundle
import com.github.lure0xaos.util.resolveName
import com.github.lure0xaos.util.toIcon
import com.github.lure0xaos.util.ui.GridBagConstraints
import com.github.lure0xaos.util.ui.JHyperlink
import com.github.lure0xaos.util.ui.JPanel
import com.github.lure0xaos.util.ui.JWrappedLabel
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.ResourceBundle
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class AboutPanel(localeHolder: LocaleHolder) : JPanel(BorderLayout(10, 10)) {
    private val resources: ResourceBundle = Res::class.getResourceBundle(AboutPanel::class, localeHolder.locale)

    init {
        add(
            JLabel(AboutPanel::class.getResource(AboutPanel::class.resolveName() + EXT_PNG).toIcon()).apply {
                border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
            },
            BorderLayout.WEST
        )
        add(JPanel(GridBagLayout()) {
            add(JLabel(resources[LC_CREATED], SwingConstants.CENTER), GridBagConstraints {
                gridy = 0
                fill = GridBagConstraints.HORIZONTAL
                ipadx = 10
                ipady = 20
                insets = Insets(10, 10, 20, 10)
                anchor = GridBagConstraints.PAGE_END
            })
            add(JHyperlink(resources[LC_LINK], LINK), GridBagConstraints {
                gridy = 2
                fill = GridBagConstraints.HORIZONTAL
                ipadx = 10
                ipady = 10
                anchor = GridBagConstraints.PAGE_START
            })
            add(JWrappedLabel(resources[LC_USAGE], SwingConstants.CENTER), GridBagConstraints {
                gridy = 1
                fill = GridBagConstraints.HORIZONTAL
                ipadx = 10
                ipady = 10
                anchor = GridBagConstraints.CENTER
            })
        }, BorderLayout.CENTER)
    }

    companion object {
        private const val EXT_PNG = ".png"

        private const val LC_CREATED = "created"
        private const val LC_LINK = "link"
        private const val LC_USAGE = "usage"
        private const val LINK = "https://github.com/lure0xaos/jrpycg"
    }
}
