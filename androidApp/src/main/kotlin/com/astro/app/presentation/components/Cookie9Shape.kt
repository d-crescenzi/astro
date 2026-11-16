package com.astro.app.presentation.components

import android.graphics.Matrix
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath

// Nine-lobed "cookie": a rounded nine-point star, built once and fitted to the drawn size.
class Cookie9Shape(
    private val innerRadius: Float = 0.82f,
    private val rounding: Float = 1f,
) : Shape {

    private val polygon = RoundedPolygon.star(
        numVerticesPerRadius = 9,
        radius = 1f,
        innerRadius = innerRadius,
        rounding = CornerRounding(rounding),
        innerRounding = CornerRounding(rounding),
    )

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val half = size.minDimension / 2f
        val path = polygon.toPath().apply {
            transform(Matrix().apply {
                setScale(half, half)
                postTranslate(size.width / 2f, size.height / 2f)
            })
        }
        return Outline.Generic(path.asComposePath())
    }
}
