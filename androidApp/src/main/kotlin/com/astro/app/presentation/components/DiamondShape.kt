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
import androidx.graphics.shapes.toPath

// Four-point diamond with softened corners, built once and fitted to the drawn size.
class DiamondShape(private val rounding: Float = 0.18f) : Shape {

    private val polygon = RoundedPolygon(
        numVertices = 4,
        radius = 1f,
        rounding = CornerRounding(rounding),
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
