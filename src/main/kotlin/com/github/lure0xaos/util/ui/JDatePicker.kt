package com.github.lure0xaos.util.ui

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Image
import java.awt.Window
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel

class JDatePicker(
    parent: Window?,
    date: LocalDate = LocalDate.now(),
    title: String? = null,
    icon: Image? = null,
    var currentLocale: Locale? = Locale.getDefault(),
    var formatter: DateTimeFormatter? =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(currentLocale),
    var additionalFormatter: DateTimeFormatter?
    = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).localizedBy(currentLocale)
) : JButton() {

    private val dialog =
        when (parent) {
            is JDialog -> JDialog(parent, findTitle(title), true)
            is JFrame -> JDialog(parent, findTitle(title), true)
            else -> error("wrong parent")
        }.apply {
            findWindow()?.iconImages = icon?.let { listOf(it) } ?: parent.findWindow()?.iconImages
        }

    var date: LocalDate = date
        set(value) {
            field = value
            refreshDisplay()
        }

    private val jLabel = JLabel("", JLabel.CENTER)
    private val buttonsHeaders: Array<JButton> = Array(7) { JButton() }
    private val buttonsDays: Array<JButton> = Array(42) { JButton() }

    init {
        dialog.apply {
            isResizable = false
            add(JPanel(GridLayout(7, 7)) {
                preferredSize = Dimension(430, 120)
                for (button in buttonsHeaders) add(button)
                for (button in buttonsDays) add(button.apply {
                    addActionListener {
                        button.text.toIntOrNull()?.apply {
                            adjustDate { it.withDayOfMonth(this) }
                        }
                        dialog.isVisible = false
                    }
                })
            }, BorderLayout.CENTER)
            add(JPanel(GridLayout(1, 5)) {
                add(JButton("<<") { adjustDate { it.minusYears(1) } })
                add(JButton("<") { adjustDate { it.minusMonths(1) } })
                add(jLabel)
                add(JButton(">") { adjustDate { it.plusMonths(1) } })
                add(JButton(">>") { adjustDate { it.plusYears(1) } })
            }, BorderLayout.SOUTH)
        }
        addActionListener {
            dialog.also { jDialog ->
                jDialog.pack()
                jDialog.setLocationRelativeTo(this.parent)
                jDialog.isVisible = true
            }
        }
        adjustDate { date }
    }

    private fun adjustDate(adjusting: (LocalDate) -> LocalDate) {
        date = adjusting(date)
    }

    private fun refreshDisplay() {
        arrayOf(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        ).map { it to it.getDisplayName(TextStyle.SHORT, currentLocale) }.forEachIndexed { x, (dow, name) ->
            buttonsHeaders[x].apply {
                isFocusPainted = false
                text = name
            }.decorateHeader(dow == DayOfWeek.SUNDAY)
        }
        for (button in buttonsDays) {
            button.apply {
                isFocusPainted = false
                text = ""
                toolTipText = ""
                decorateButton()
            }
        }
        val dayOfWeek = date.dayOfWeek.value
        val daysInMonth = date.lengthOfMonth()
        for (day in 1..daysInMonth) {
            val currentDate = date.withDayOfMonth(day)
            val isToday = LocalDate.now() == currentDate
            val isSelected = date == currentDate
            buttonsDays[dayOfWeek + day].apply {
                text = "$day"
                toolTipText = currentDate.format(formatter)
                decorateButton()
                if (isToday) decorateToday()
                if (isSelected) decorateSelected()
            }
        }
        text = date.format(formatter)
        toolTipText = date.format(additionalFormatter)
        jLabel.text = date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    private fun JButton.decorateButton() {
        background = Color.WHITE
        foreground = Color.BLACK
    }

    private fun JButton.decorateSelected() {
        background = Color.LIGHT_GRAY
        foreground = foreground
    }

    private fun JButton.decorateToday() {
        background = Color.WHITE
        foreground = Color.GREEN
    }

    private fun JButton.decorateHeader(isHoliday: Boolean) {
        background = Color.LIGHT_GRAY
        foreground = if (isHoliday) Color.RED else Color.BLACK
    }

}
