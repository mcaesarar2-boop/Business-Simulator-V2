package com.caesar.gametycoon.engine.fix

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Audit Fix: ChartSafetyUtils
 *
 * Resolves the Canvas rendering crash caused by division-by-zero (prices.size - 1 = 0)
 * when a stock, cryptocurrency, or asset portfolio has exactly one historical price point.
 */
object ChartSafetyUtils {

    /**
     * Safely projects a list of financial price points onto a 2D Canvas coordinate space.
     * Prevents NaN, Infinity, and division-by-zero crashes.
     *
     * @param prices Historical price series
     * @param canvasWidth Width of drawing area
     * @param canvasHeight Height of drawing area
     * @param verticalPaddingFraction Fraction reserved for top/bottom padding (default 0.09)
     * @param effectiveHeightFraction Fraction of height used for data points (default 0.82)
     */
    fun calculateSafeChartPoints(
        prices: List<Double>,
        canvasWidth: Float,
        canvasHeight: Float,
        verticalPaddingFraction: Float = 0.09f,
        effectiveHeightFraction: Float = 0.82f
    ): List<Offset> {
        if (prices.isEmpty() || canvasWidth <= 0f || canvasHeight <= 0f) {
            return emptyList()
        }

        // Safety Guard for Single Point: span horizontally across canvas center to prevent NaN
        if (prices.size == 1) {
            val centerY = canvasHeight / 2f
            return listOf(
                Offset(0f, centerY),
                Offset(canvasWidth, centerY)
            )
        }

        val maxPrice = prices.maxOrNull() ?: 1.0
        val minPrice = prices.minOrNull() ?: 0.0
        val priceRange = if (maxPrice - minPrice <= 0.00001) 1.0 else maxPrice - minPrice

        val denominator = (prices.size - 1).coerceAtLeast(1)

        return prices.mapIndexed { index, price ->
            val x = (index.toDouble() / denominator * canvasWidth).toFloat()
            val normalizedPrice = (price - minPrice) / priceRange
            val y = (canvasHeight - (normalizedPrice * canvasHeight * effectiveHeightFraction + canvasHeight * verticalPaddingFraction)).toFloat()

            Offset(
                x = if (x.isNaN() || x.isInfinite()) 0f else x.coerceIn(0f, canvasWidth),
                y = if (y.isNaN() || y.isInfinite()) canvasHeight / 2f else y.coerceIn(0f, canvasHeight)
            )
        }
    }

    /**
     * Safely draws a financial trend line onto a Canvas DrawScope.
     */
    fun DrawScope.drawSafeFinancialLine(
        prices: List<Double>,
        lineColor: Color = Color(0xFF10B981),
        strokeWidth: Float = 2.5f
    ) {
        if (prices.size <= 1) {
            // Draw a neutral flat baseline if single price or empty
            if (prices.isNotEmpty()) {
                val midY = size.height / 2f
                drawLine(
                    color = lineColor.copy(alpha = 0.5f),
                    start = Offset(0f, midY),
                    end = Offset(size.width, midY),
                    strokeWidth = strokeWidth
                )
            }
            return
        }

        val points = calculateSafeChartPoints(
            prices = prices,
            canvasWidth = size.width,
            canvasHeight = size.height
        )

        if (points.size < 2) return

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokeWidth)
        )
    }

    /**
     * Validates whether a price collection is safe to render on a chart without fallback guards.
     */
    fun isChartRenderable(prices: List<Double>?): Boolean {
        return !prices.isNullOrEmpty() && prices.size >= 2
    }
}
