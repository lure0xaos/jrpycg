package com.github.lure0xaos.util

import java.awt.geom.Rectangle2D

object Fit {

    fun fit(
        inner: Rectangle2D, outer: Rectangle2D,
        fits: FITS = FITS.MIN,
        anchorVertical: Double = 0.5,
        anchorHorizontal: Double = 0.5,
    ): Rectangle2D {
        val fit = Rectangle2D.Double()
        if (when (fits) {
                FITS.MIN -> outer.width < outer.height
                FITS.MAX -> outer.width > outer.height
                FITS.HEIGHT -> true
                FITS.WIDTH -> false
            }
        ) {
            fit.width = outer.width
            fit.height = outer.width * inner.height / inner.width
        } else {
            fit.height = outer.height
            fit.width = outer.height * inner.width / inner.height
        }
        fit.x = (outer.width - fit.width) * anchorHorizontal.toRanged(0.0, 1.0) + outer.x
        fit.y = (outer.height - fit.height) * anchorVertical.toRanged(0.0, 1.0) + outer.y
        require(doubleApproximates(fit.width / fit.height, inner.width / inner.height))
        return fit
    }

    enum class FITS { MIN, MAX, WIDTH, HEIGHT }
}
