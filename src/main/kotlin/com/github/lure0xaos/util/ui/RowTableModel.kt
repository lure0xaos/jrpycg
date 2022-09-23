package com.github.lure0xaos.util.ui

import java.awt.Component
import java.awt.Insets
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.text.DateFormat
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor
import java.util.Collections
import java.util.Date
import java.util.Enumeration
import java.util.EventObject
import java.util.Locale
import javax.swing.DefaultListSelectionModel
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.UIManager
import javax.swing.border.Border
import javax.swing.border.EmptyBorder
import javax.swing.event.CellEditorListener
import javax.swing.event.ChangeEvent
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.event.TableColumnModelEvent
import javax.swing.event.TableColumnModelListener
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.plaf.UIResource
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumn
import javax.swing.table.TableColumnModel
import javax.swing.table.TableModel
import kotlin.math.max
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf

class RowTableModel<T : Any>(
    private val properties: List<Pair<String, KProperty1<T, Any?>>> = listOf(),
    private val actions: List<Pair<Pair<String?, Icon?>, (RowTableModel<T>, Int, T) -> Unit>> = listOf(),
    data: List<T> = listOf(),
    locale: Locale? = Locale.getDefault(),
    factory: (String?, Icon?) ->
    JButton = { label, icon -> JButton(label, icon).apply { margin = Insets(0, 0, 0, 0) } }
) : TableModel, TableColumnModel, ListSelectionListener, PropertyChangeListener {

    constructor(properties: List<Pair<String, KProperty1<T, Any?>>> = listOf(), data: List<T> = listOf()) :
            this(properties, listOf(), data, Locale.getDefault())

    private val columns: List<TableColumn> = properties.mapIndexed { index, (header, property) ->
        TableColumn(index, 75, getCellRendererFor(getPropertyClass(property), locale), null).apply {
            headerValue = header
            addPropertyChangeListener(this@RowTableModel)
        }
    } + listOf(TableColumn(
        properties.size, 75,
        RowTableCellRenderer(this, factory), RowTableCellEditor(this, factory)
    ).apply {
        headerValue = ""
        addPropertyChangeListener(this@RowTableModel)
    })

    inner class RowTableCellRenderer(
        private val rowTableModel: RowTableModel<T>,
        private val factory: (String?, Icon?) -> JButton
    ) : TableCellRenderer {
        override fun getTableCellRendererComponent(
            table: JTable,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component =
            createButtonColumn(rowTableModel, row, factory)
    }

    inner class RowTableCellEditor(
        private val rowTableModel: RowTableModel<T>,
        private val factory: (String?, Icon?) -> JButton
    ) : TableCellEditor {

        override fun getTableCellEditorComponent(
            table: JTable,
            value: Any?,
            isSelected: Boolean,
            row: Int,
            column: Int
        ): Component =
            createButtonColumn(rowTableModel, row, factory)

        private val cellEditorListeners: MutableList<CellEditorListener> = mutableListOf()

        override fun getCellEditorValue(): Any =
            createButtonColumn(rowTableModel, 0, factory)

        override fun isCellEditable(anEvent: EventObject): Boolean {
            return true
        }

        override fun shouldSelectCell(anEvent: EventObject): Boolean {
            return true
        }

        override fun stopCellEditing(): Boolean {
            cellEditorListeners.forEach {
                it.editingStopped(ChangeEvent(this@RowTableModel))
            }
            return true
        }

        override fun cancelCellEditing() {
            cellEditorListeners.forEach {
                it.editingCanceled(ChangeEvent(this@RowTableModel))
            }
        }

        override fun addCellEditorListener(l: CellEditorListener) {
            cellEditorListeners += l
        }

        override fun removeCellEditorListener(l: CellEditorListener) {
            cellEditorListeners.remove(l)
        }
    }


    companion object {
        private fun <T : Any> createButtonColumn(
            rowTableModel: RowTableModel<T>, index: Int,
            factory: (String?, Icon?) -> JButton
        ): JComponent {
            return JButtonPanel(
                rowTableModel.actions.map { (labelIcon, action) ->
                    val (label, icon) = labelIcon
                    (factory(label, icon).apply {
                        margin = Insets(0, 0, 0, 0)
                        toolTipText = text
                        addActionListener {
                            action(rowTableModel, index, rowTableModel.getRow(index))
                        }
                    })
                })
        }

        private fun getCellRendererFor(type: KClass<*>, locale: Locale?): TableCellRenderer? =
            when {
                type == Double::class -> DoubleRenderer()
                type == Boolean::class -> BooleanRenderer()
                type == Date::class -> DateRenderer()
                type.isSubclassOf(TemporalAccessor::class) -> TemporalRenderer(
                    when (type) {
                        Instant::class ->
                            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.FULL).withLocale(locale)

                        ZonedDateTime::class ->
                            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.FULL).withLocale(locale)

                        LocalDateTime::class ->
                            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.FULL).withLocale(locale)

                        LocalDate::class -> DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
                        LocalTime::class -> DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL).withLocale(locale)
                        else -> error("unsupported format $type")
                    }
                )

                else -> null
            }

        internal open class NumberRenderer : DefaultTableCellRenderer.UIResource() {
            init {
                horizontalAlignment = RIGHT
            }
        }

        internal class DoubleRenderer(var formatter: NumberFormat = NumberFormat.getInstance()) : NumberRenderer() {
            override fun setValue(value: Any?) {
                text = value?.let { formatter.format(it) } ?: ""
            }
        }

        internal class BooleanRenderer : JCheckBox(), TableCellRenderer, UIResource {
            init {
                horizontalAlignment = CENTER
                isBorderPainted = true
            }

            override fun getTableCellRendererComponent(
                table: JTable, value: Any?,
                isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
            ): Component =
                apply {
                    if (isSelected) {
                        foreground = table.selectionForeground
                        super.setBackground(table.selectionBackground)
                    } else {
                        foreground = table.foreground
                        background = table.background
                    }
                    setSelected(value?.toString()?.toBoolean() ?: false)
                    border = if (hasFocus) UIManager.getBorder("Table.focusCellHighlightBorder") else noFocusBorder
                }

            companion object {
                private val noFocusBorder: Border = EmptyBorder(1, 1, 1, 1)
            }
        }

        internal class DateRenderer(var formatter: DateFormat = DateFormat.getDateInstance()) :
            DefaultTableCellRenderer.UIResource() {
            public override fun setValue(value: Any?) {
                text = value?.let { formatter.format(it) } ?: ""
            }
        }

        internal class TemporalRenderer(var formatter: DateTimeFormatter) :
            DefaultTableCellRenderer.UIResource() {
            public override fun setValue(value: Any?) {
                text = value?.let { formatter.format(it as TemporalAccessor) } ?: ""
            }
        }

        private fun getPropertyClass(property: KProperty1<*, Any?>): KClass<*> =
            property.returnType.classifier as KClass<*>

    }

    private val model: MutableList<T> = data.toMutableList()

    private var pColumnMargin: Int = 0

    private var pColumnSelectionAllowed: Boolean = false

    private var listSelectionModel: ListSelectionModel = DefaultListSelectionModel()

    private val tableColumnModelListeners: MutableList<TableColumnModelListener> = mutableListOf()

    private val tableModelListeners: MutableList<TableModelListener> = mutableListOf()

    override fun getRowCount(): Int =
        model.size

    override fun addColumn(aColumn: TableColumn) {
        tableColumnModelListeners.forEach {
            it.columnAdded(TableColumnModelEvent(this, aColumn.modelIndex, aColumn.modelIndex))
        }
    }

    override fun removeColumn(column: TableColumn) {
        tableColumnModelListeners.forEach {
            it.columnRemoved(TableColumnModelEvent(this, column.modelIndex, column.modelIndex))
        }
    }

    override fun moveColumn(columnIndex: Int, newIndex: Int) {
        tableColumnModelListeners.forEach {
            it.columnMoved(TableColumnModelEvent(this, columnIndex, newIndex))
        }
    }

    override fun setColumnMargin(newMargin: Int) {
        pColumnMargin = newMargin
        tableColumnModelListeners.forEach {
            it.columnMarginChanged(ChangeEvent(this))
        }
    }

    override fun getColumnCount(): Int =
        columns.size

    override fun getColumns(): Enumeration<TableColumn> =
        Collections.enumeration(columns)

    override fun getColumnIndex(columnIdentifier: Any?): Int =
        columns.firstOrNull { it.identifier == columnIdentifier }?.modelIndex ?: -1

    override fun getColumn(columnIndex: Int): TableColumn =
        columns[columnIndex]

    override fun getColumnMargin(): Int =
        pColumnMargin

    override fun getColumnIndexAtX(xPosition: Int): Int {
        var x = 0
        columns.forEachIndexed { index, tableColumn ->
            if (xPosition >= x && xPosition <= x + tableColumn.width) return index
            x += tableColumn.width
        }
        return -1
    }

    override fun getTotalColumnWidth(): Int =
        columns.sumOf { it.width }

    override fun setColumnSelectionAllowed(flag: Boolean) {
        pColumnSelectionAllowed = flag
    }

    override fun getColumnSelectionAllowed(): Boolean =
        pColumnSelectionAllowed

    override fun getSelectedColumns(): IntArray =
        listSelectionModel.selectedIndices

    override fun getSelectedColumnCount(): Int =
        listSelectionModel.selectedItemsCount

    override fun setSelectionModel(newModel: ListSelectionModel) {
        listSelectionModel.removeListSelectionListener(this)
        listSelectionModel = newModel.apply {
            addListSelectionListener(this@RowTableModel)
        }
    }

    override fun getSelectionModel(): ListSelectionModel =
        listSelectionModel

    override fun addColumnModelListener(x: TableColumnModelListener) {
        tableColumnModelListeners += x
    }

    override fun removeColumnModelListener(x: TableColumnModelListener) {
        tableColumnModelListeners.remove(x)
    }

    override fun getColumnName(columnIndex: Int): String =
        properties[columnIndex].first

    override fun getColumnClass(columnIndex: Int): Class<*> =
        getPropertyClass(properties[columnIndex].second).java

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean =
        columns[columnIndex].cellEditor != null

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? =
        if (columnIndex == properties.size) columns[columnIndex].cellRenderer
        else properties[columnIndex].second.get(model[rowIndex])

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        properties[columnIndex].second
            .also { require(it is KMutableProperty1<T, Any?>) }
            .let { it as KMutableProperty1<T, Any?> }
            .also { it.set(model[rowIndex], aValue) }
    }

    override fun addTableModelListener(l: TableModelListener) {
        tableModelListeners += l
    }

    override fun removeTableModelListener(l: TableModelListener) {
        tableModelListeners.remove(l)
    }

    fun getAll(): List<T> =
        model

    fun setAll(data: List<T>) {
        val lastRow = max(model.size, data.size)
        model.forEach { removeRow(it) }
        data.forEach { addRow(it) }
        tableModelListeners.forEach {
            it.tableChanged(
                TableModelEvent(this, 0, lastRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE)
            )
        }
    }

    fun setRow(rowIndex: Int, data: T) {
        require(rowIndex < model.size)
        model[rowIndex] = data
        tableModelListeners.forEach {
            it.tableChanged(
                TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE)
            )
        }
    }

    fun getRow(rowIndex: Int): T {
        require(rowIndex < model.size)
        return model[rowIndex]
    }

    fun addRow(data: T) {
        val rowIndex = model.size
        model += data
        tableModelListeners.forEach {
            it.tableChanged(
                TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)
            )
        }
    }

    fun insertRow(rowIndex: Int, data: T) {
        if (rowIndex == model.size) {
            addRow(data)
            return
        }
        require(rowIndex < model.size)
        model[rowIndex] = data
        tableModelListeners.forEach {
            it.tableChanged(
                TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT)
            )
        }
    }

    fun removeRow(rowIndex: Int) {
        require(rowIndex < model.size)
        model.removeAt(rowIndex)
        tableModelListeners.forEach {
            it.tableChanged(
                TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE)
            )
        }
    }

    fun removeRow(row: T) {
        val rowIndex = model.indexOf(row)
        require(rowIndex < model.size)
        model.removeAt(rowIndex)
        tableModelListeners.forEach {
            it.tableChanged(
                TableModelEvent(this, rowIndex, rowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE)
            )
        }
    }

    override fun valueChanged(e: ListSelectionEvent) {
        tableColumnModelListeners.forEach {
            it.columnSelectionChanged(e)
        }
    }

    override fun propertyChange(evt: PropertyChangeEvent) {
        if (evt.propertyName === "width" || evt.propertyName === "preferredWidth") {
            tableColumnModelListeners.forEach {
                it.columnMarginChanged(ChangeEvent(this))
            }
        }
    }

}
