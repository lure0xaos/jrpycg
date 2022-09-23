package com.github.lure0xaos.util.ui

import com.github.lure0xaos.util.Fit
import java.awt.AlphaComposite
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.LayoutManager
import java.awt.RenderingHints
import java.awt.geom.Rectangle2D
import javax.swing.JPanel

class JImagePanel(
    private val image: Image?,
    private val opacity: Double = 0.3,
    private val fits: Fit.FITS = Fit.FITS.MIN,
    private val anchorHorizontal: Double = 0.5,
    private val anchorVertical: Double = 0.5,
    layout: LayoutManager? = null,
    customizer: JPanel.() -> Unit = {}
) : JPanel(layout) {

    override fun paint(g: Graphics) {
        super.paint(g)
        if (g !is Graphics2D || image == null) return
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity.toFloat())
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val fit: Rectangle2D = Fit.fit(
            Rectangle2D.Double(0.0, 0.0, image.width.toDouble(), image.height.toDouble()),
            Rectangle2D.Double(0.0, 0.0, size.width.toDouble(), size.height.toDouble()),
            fits,
            anchorVertical,
            anchorHorizontal
        )
        g.drawImage(
            image, fit.x.toInt(), fit.y.toInt(), (fit.x + fit.width).toInt(), (fit.y + fit.height).toInt(),
            Rectangle2D.Double(0.0, 0.0, image.width.toDouble(), image.height.toDouble()).x.toInt(),
            Rectangle2D.Double(0.0, 0.0, image.width.toDouble(), image.height.toDouble()).y.toInt(),
            Rectangle2D.Double(0.0, 0.0, image.width.toDouble(), image.height.toDouble()).width.toInt(),
            Rectangle2D.Double(0.0, 0.0, image.width.toDouble(), image.height.toDouble()).height.toInt(),
            null
        )
    }

    init {
        isOpaque = false
        if (image != null) preferredSize = Dimension(image.width, image.height)
        customizer()
    }
}
