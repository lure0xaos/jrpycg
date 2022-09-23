package com.github.lure0xaos.util.ui

import com.github.lure0xaos.util.log.Log
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.Insets
import java.awt.LayoutManager2
import java.awt.Rectangle

internal class GraphPaperLayout(
    gridSize: Dimension = Dimension(1, 1),
    private val hGap: Int = 0,
    private val vGap: Int = 0
) :
    LayoutManager2 {

    constructor(width: Int, height: Int, hGap: Int = 0, vGap: Int = 0) : this(Dimension(width, height), hGap, vGap)

    private val gridSize: Dimension = Dimension(gridSize.width, gridSize.height)
        get() = Dimension(field)

    private val table: MutableMap<Component, Rectangle> = mutableMapOf()

    override fun addLayoutComponent(name: String, comp: Component) {}

    override fun removeLayoutComponent(comp: Component) {
        table.remove(comp)
    }

    override fun preferredLayoutSize(parent: Container): Dimension = getLayoutSize(parent, true)

    override fun minimumLayoutSize(parent: Container): Dimension = getLayoutSize(parent, false)

    private fun getLayoutSize(parent: Container, isPreferred: Boolean): Dimension {
        val insets = parent.insets
        val largestSize = getLargestCellSize(parent, isPreferred)
        return Dimension(
            largestSize.width * gridSize.width + hGap * (gridSize.width + 1) + insets.left + insets.right,
            largestSize.height * gridSize.height + vGap * (gridSize.height + 1) + insets.top + insets.bottom
        )
    }

    private fun getLargestCellSize(parent: Container, isPreferred: Boolean): Dimension {
        val maxCellSize = Dimension(0, 0)
        for (component: Component in parent.components) {
            if (component in table) {
                val rect = table[component]!!
                val componentSize: Dimension = if (isPreferred) component.preferredSize else component.minimumSize
                maxCellSize.width = maxCellSize.width.coerceAtLeast(componentSize.width / rect.width)
                maxCellSize.height = maxCellSize.height.coerceAtLeast(componentSize.height / rect.height)
            }
        }
        return maxCellSize
    }

    override fun layoutContainer(parent: Container) {
        synchronized(parent.treeLock) {
            if (parent.components.isEmpty()) return
            val insets: Insets = parent.insets
            val size: Dimension = parent.size
            val totalW: Int = size.width - (insets.left + insets.right)
            val totalH: Int = size.height - (insets.top + insets.bottom)
            val totalCellW: Int = totalW / gridSize.width
            val totalCellH: Int = totalH / gridSize.height
            val cellW: Int = (totalCellW - (gridSize.width + 1) * hGap
                    / gridSize.width)
            val cellH: Int = (totalCellH - (gridSize.height + 1) * vGap
                    / gridSize.height)
            Log.debug { "layout parent: ${parent.size}" }
            for (component: Component in parent.components) {
                if (component in table) {
                    val rect: Rectangle = table[component]!!
                    component.setBounds(
                        insets.left + totalCellW * rect.x + hGap,
                        insets.top + totalCellH * rect.y + vGap,
                        cellW * rect.width - hGap,
                        cellH * rect.height - vGap
                    )
                    component.bounds.also {
                        if ((it.x + it.width > parent.width) || (it.y + it.height > parent.height)) {
                            error("out of bounds: $it ${parent.size}")
                        }
                    }.also {
                        Log.debug { "layout component: $component ${component.bounds}" }
                    }
                }
            }
        }
    }

    override fun addLayoutComponent(comp: Component, constraints: Any) {
        require(constraints is Rectangle) { "cannot add to layout: constraint must be a Rectangle" }
        require(constraints.width > 0 && constraints.height > 0) {
            "cannot add to layout: rectangle must have positive width and height"
        }
        require(constraints.x >= 0 && constraints.y >= 0) {
            "cannot add to layout: rectangle x and y must be non-negative"
        }
        require(constraints.x + constraints.width <= gridSize.width && constraints.y + constraints.height <= gridSize.height) {
            "cannot add to layout: component must fit into grid"
        }
        table[comp] = Rectangle(constraints)
    }

    override fun maximumLayoutSize(target: Container): Dimension = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

    override fun getLayoutAlignmentX(target: Container): Float = 0.5f

    override fun getLayoutAlignmentY(target: Container): Float = 0.5f

    override fun invalidateLayout(target: Container) {}

    init {
        require(gridSize.width > 0 && gridSize.height > 0) { "dimensions must be greater than zero" }
    }
}
