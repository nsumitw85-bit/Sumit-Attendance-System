package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium, Professional Mobile App Logo for "Sumit Attendance System"
 *
 * Requirements implemented:
 * - Perfectly circular logo inspired by the Ashoka Chakra style.
 * - Dark midnight navy blue circular background (#0B192C, #060E1A).
 * - Elegant Ashoka Chakra-inspired 24-spoke circular radial ring.
 * - Large capital letter "S" in the center with 3D metallic luxury gold finish.
 * - Upper curve text: "SUMIT ATTENDANCE SYSTEM" in uppercase bold English letters along the circular ring.
 * - Modern, minimalist, professional, corporate vector design.
 * - Luxury gold (#FFE082, #FFD54F, #FFB300, #D4AF37) and dark navy combination.
 * - Exportable as a high-resolution 1024x1024 PNG / Bitmap.
 */

// Premium Brand Palette Constants
val GoldLight = Color(0xFFFFF9C4)
val GoldBright = Color(0xFFFFE082)
val GoldCore = Color(0xFFFFD54F)
val GoldDeep = Color(0xFFFFB300)
val GoldDark = Color(0xFFD4AF37)
val GoldShadow = Color(0xFF8D6E63)

val NavyDark = Color(0xFF0B192C)
val NavyMidnight = Color(0xFF060E1A)
val NavyDeep = Color(0xFF000814)

@Composable
fun SumitAttendanceLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    showCurvedText: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        modifier.size(size).clickable { onClick() }
    } else {
        modifier.size(size)
    }

    Box(
        modifier = clickableModifier.testTag("sumit_attendance_logo"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val center = Offset(canvasSize / 2f, canvasSize / 2f)
            val radius = canvasSize / 2f * 0.94f

            // 1. Draw Circular Dark Navy Blue Background with subtle radial depth
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NavyDark, NavyMidnight, NavyDeep),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 2. Outer Luxury Gold Ring Border
            val goldBrush = Brush.linearGradient(
                colors = listOf(GoldLight, GoldCore, GoldDeep, GoldDark, GoldBright),
                start = Offset(center.x - radius, center.y - radius),
                end = Offset(center.x + radius, center.y + radius)
            )

            val outerStrokeWidth = canvasSize * 0.024f
            drawCircle(
                brush = goldBrush,
                radius = radius - outerStrokeWidth / 2f,
                center = center,
                style = Stroke(width = outerStrokeWidth)
            )

            // 3. Inner Concentric Bevel Gold Ring
            val innerRingRadius = radius * 0.82f
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(GoldDark, GoldBright, GoldDeep),
                    start = Offset(center.x, center.y - innerRingRadius),
                    end = Offset(center.x, center.y + innerRingRadius)
                ),
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = canvasSize * 0.012f)
            )

            // 4. Ashoka Chakra 24 Radiating Geometric Gold Spokes & Outer Track
            val chakraRadius = radius * 0.76f
            val hubRadius = radius * 0.38f

            drawCircle(
                color = GoldDark.copy(alpha = 0.5f),
                radius = chakraRadius,
                center = center,
                style = Stroke(width = canvasSize * 0.008f)
            )

            drawCircle(
                color = GoldBright.copy(alpha = 0.6f),
                radius = hubRadius,
                center = center,
                style = Stroke(width = canvasSize * 0.010f)
            )

            // 24 Radial Spokes with subtle diamond points
            val spokeCount = 24
            for (i in 0 until spokeCount) {
                val angleRad = (i * 360f / spokeCount) * (PI / 180.0)
                val startX = (center.x + hubRadius * cos(angleRad)).toFloat()
                val startY = (center.y + hubRadius * sin(angleRad)).toFloat()
                val endX = (center.x + chakraRadius * cos(angleRad)).toFloat()
                val endY = (center.y + chakraRadius * sin(angleRad)).toFloat()

                drawLine(
                    color = if (i % 2 == 0) GoldBright.copy(alpha = 0.75f) else GoldDeep.copy(alpha = 0.45f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (i % 6 == 0) canvasSize * 0.012f else canvasSize * 0.006f
                )

                // Perimetric Chakra beads
                if (i % 2 == 0) {
                    val beadRadius = canvasSize * 0.012f
                    drawCircle(
                        color = GoldCore,
                        radius = beadRadius,
                        center = Offset(endX, endY)
                    )
                }
            }

            // 5. Draw Curved Circular Text "SUMIT ATTENDANCE SYSTEM" around outer ring
            if (showCurvedText) {
                drawContext.canvas.nativeCanvas.apply {
                    val textPath = Path()
                    val textRadius = radius * 0.90f
                    val oval = android.graphics.RectF(
                        center.x - textRadius,
                        center.y - textRadius,
                        center.x + textRadius,
                        center.y + textRadius
                    )

                    // Path curving along top arc (from ~200 deg to -20 deg)
                    textPath.addArc(oval, 186f, 168f)

                    val textPaint = Paint().apply {
                        isAntiAlias = true
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        textSize = canvasSize * 0.062f
                        color = android.graphics.Color.parseColor("#FFE082")
                        textAlign = Paint.Align.CENTER
                        letterSpacing = 0.12f
                        setShadowLayer(canvasSize * 0.01f, 0f, 0f, android.graphics.Color.parseColor("#80000000"))
                    }

                    drawTextOnPath(
                        "SUMIT ATTENDANCE SYSTEM",
                        textPath,
                        0f,
                        0f,
                        textPaint
                    )
                }
            }

            // 6. Draw Large Center Capital Letter "S" with 3D Metallic Luxury Gold Finish
            drawCenterGoldenS(
                center = center,
                canvasSize = canvasSize
            )
        }
    }
}

/**
 * Draws a large, bold, polished metallic gold letter "S" in the center with 3D bevel and specular glow
 */
private fun DrawScope.drawCenterGoldenS(center: Offset, canvasSize: Float) {
    val scale = canvasSize / 100f
    drawContext.canvas.nativeCanvas.apply {
        val sPaint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            setShadowLayer(scale * 3.5f, 0f, scale * 2f, android.graphics.Color.parseColor("#B0000000"))
        }

        // Create Path for the bold letter S centered at (center.x, center.y)
        // Scaled to fit gracefully inside the hub
        val sPath = Path().apply {
            val cx = center.x
            val cy = center.y

            // Standard relative coordinates scaled around center
            val dx = cx - 54f * scale
            val dy = cy - 54f * scale

            moveTo(58.5f * scale + dx, 37.5f * scale + dy)
            cubicTo(
                55.5f * scale + dx, 36f * scale + dy,
                52.5f * scale + dx, 36f * scale + dy,
                49.5f * scale + dx, 37.5f * scale + dy
            )
            cubicTo(
                46f * scale + dx, 39.2f * scale + dy,
                44f * scale + dx, 42f * scale + dy,
                44f * scale + dx, 45.5f * scale + dy
            )
            cubicTo(
                44f * scale + dx, 50f * scale + dy,
                48f * scale + dx, 52.5f * scale + dy,
                53.5f * scale + dx, 54f * scale + dy
            )
            cubicTo(
                58.5f * scale + dx, 55.5f * scale + dy,
                62f * scale + dx, 57.5f * scale + dy,
                62f * scale + dx, 61.5f * scale + dy
            )
            cubicTo(
                62f * scale + dx, 65.5f * scale + dy,
                58.5f * scale + dx, 68.5f * scale + dy,
                53.5f * scale + dx, 68.5f * scale + dy
            )
            cubicTo(
                48.5f * scale + dx, 68.5f * scale + dy,
                44.5f * scale + dx, 66f * scale + dy,
                42.5f * scale + dx, 63.5f * scale + dy
            )
            lineTo(39.5f * scale + dx, 67.5f * scale + dy)
            cubicTo(
                42.5f * scale + dx, 71f * scale + dy,
                47.5f * scale + dx, 73.5f * scale + dy,
                53.5f * scale + dx, 73.5f * scale + dy
            )
            cubicTo(
                61.5f * scale + dx, 73.5f * scale + dy,
                67.5f * scale + dx, 69f * scale + dy,
                67.5f * scale + dx, 61.5f * scale + dy
            )
            cubicTo(
                67.5f * scale + dx, 55.5f * scale + dy,
                63f * scale + dx, 52.5f * scale + dy,
                57f * scale + dx, 51f * scale + dy
            )
            cubicTo(
                52f * scale + dx, 49.5f * scale + dy,
                49.5f * scale + dx, 48f * scale + dy,
                49.5f * scale + dx, 45f * scale + dy
            )
            cubicTo(
                49.5f * scale + dx, 42.5f * scale + dy,
                51.5f * scale + dx, 40.5f * scale + dy,
                54.5f * scale + dx, 40.5f * scale + dy
            )
            cubicTo(
                57.5f * scale + dx, 40.5f * scale + dy,
                60f * scale + dx, 41.8f * scale + dy,
                62f * scale + dx, 44f * scale + dy
            )
            lineTo(65.5f * scale + dx, 40.5f * scale + dy)
            cubicTo(
                63.5f * scale + dx, 38f * scale + dy,
                61f * scale + dx, 36.5f * scale + dy,
                58.5f * scale + dx, 37.5f * scale + dy
            )
            close()
        }

        // Multi-stop Metallic Gold Gradient Shader
        sPaint.shader = LinearGradient(
            center.x - 15f * scale, center.y - 20f * scale,
            center.x + 15f * scale, center.y + 20f * scale,
            intArrayOf(
                android.graphics.Color.parseColor("#FFFDE7"), // Lightest glint
                android.graphics.Color.parseColor("#FFE082"), // Pale gold
                android.graphics.Color.parseColor("#FFD54F"), // Pure gold
                android.graphics.Color.parseColor("#FFB300"), // Rich amber
                android.graphics.Color.parseColor("#FFA000"), // Deep gold
                android.graphics.Color.parseColor("#D4AF37"), // Metallic gold
                android.graphics.Color.parseColor("#FFF9C4")  // Highlight
            ),
            floatArrayOf(0.0f, 0.18f, 0.38f, 0.60f, 0.78f, 0.90f, 1.0f),
            Shader.TileMode.CLAMP
        )

        drawPath(sPath, sPaint)

        // Luxury Stroke Edge to give bevel crispness
        val edgePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = scale * 0.7f
            color = android.graphics.Color.parseColor("#FFF9C4")
        }
        drawPath(sPath, edgePaint)
    }
}

/**
 * Generates a high-resolution 1024x1024 Bitmap of the Sumit Attendance System Logo
 * with transparent background for export, app branding, and PDF generation.
 */
fun createSumitLogoBitmap(
    size: Int = 1024,
    transparentBackground: Boolean = true
): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val center = Offset(size / 2f, size / 2f)
    val radius = (size / 2f) * 0.92f

    // 1. Background
    val bgPaint = Paint().apply {
        isAntiAlias = true
        shader = android.graphics.RadialGradient(
            center.x, center.y, radius,
            intArrayOf(
                android.graphics.Color.parseColor("#0F1E36"),
                android.graphics.Color.parseColor("#0B192C"),
                android.graphics.Color.parseColor("#060E1A")
            ),
            floatArrayOf(0.0f, 0.65f, 1.0f),
            Shader.TileMode.CLAMP
        )
    }

    if (!transparentBackground) {
        canvas.drawColor(android.graphics.Color.WHITE)
    }
    canvas.drawCircle(center.x, center.y, radius, bgPaint)

    // 2. Outer Beveled Gold Rim
    val outerGoldPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = size * 0.026f
        shader = LinearGradient(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
            intArrayOf(
                android.graphics.Color.parseColor("#FFF9C4"),
                android.graphics.Color.parseColor("#FFD54F"),
                android.graphics.Color.parseColor("#FFB300"),
                android.graphics.Color.parseColor("#D4AF37"),
                android.graphics.Color.parseColor("#FFE082")
            ),
            null,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawCircle(center.x, center.y, radius - outerGoldPaint.strokeWidth / 2f, outerGoldPaint)

    // 3. Inner Gold Ring
    val innerRingRadius = radius * 0.83f
    val innerGoldPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = size * 0.012f
        color = android.graphics.Color.parseColor("#D4AF37")
    }
    canvas.drawCircle(center.x, center.y, innerRingRadius, innerGoldPaint)

    // 4. Ashoka Chakra 24 Spokes & Radiance
    val chakraRadius = radius * 0.77f
    val hubRadius = radius * 0.38f

    val trackPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = size * 0.008f
        color = android.graphics.Color.parseColor("#90FFD54F")
    }
    canvas.drawCircle(center.x, center.y, chakraRadius, trackPaint)
    canvas.drawCircle(center.x, center.y, hubRadius, trackPaint)

    val spokePaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = size * 0.007f
        color = android.graphics.Color.parseColor("#B0FFD54F")
    }
    val majorSpokePaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = size * 0.014f
        color = android.graphics.Color.parseColor("#FFFFE082")
    }
    val beadPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.parseColor("#FFFFD54F")
    }

    val spokeCount = 24
    for (i in 0 until spokeCount) {
        val angleRad = (i * 360f / spokeCount) * (PI / 180.0)
        val startX = (center.x + hubRadius * cos(angleRad)).toFloat()
        val startY = (center.y + hubRadius * sin(angleRad)).toFloat()
        val endX = (center.x + chakraRadius * cos(angleRad)).toFloat()
        val endY = (center.y + chakraRadius * sin(angleRad)).toFloat()

        val paintToUse = if (i % 6 == 0) majorSpokePaint else spokePaint
        canvas.drawLine(startX, startY, endX, endY, paintToUse)

        if (i % 2 == 0) {
            canvas.drawCircle(endX, endY, size * 0.012f, beadPaint)
        }
    }

    // 5. Curved Text "SUMIT ATTENDANCE SYSTEM"
    val textPath = Path()
    val textRadius = radius * 0.905f
    val oval = android.graphics.RectF(
        center.x - textRadius,
        center.y - textRadius,
        center.x + textRadius,
        center.y + textRadius
    )
    textPath.addArc(oval, 186f, 168f)

    val textPaint = Paint().apply {
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = size * 0.060f
        color = android.graphics.Color.parseColor("#FFE082")
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.12f
        setShadowLayer(size * 0.008f, 0f, 0f, android.graphics.Color.parseColor("#90000000"))
    }
    canvas.drawTextOnPath("SUMIT ATTENDANCE SYSTEM", textPath, 0f, 0f, textPaint)

    // 6. Large Central Metallic Gold "S"
    val scale = size / 100f
    val sPath = Path().apply {
        val dx = center.x - 54f * scale
        val dy = center.y - 54f * scale

        moveTo(58.5f * scale + dx, 37.5f * scale + dy)
        cubicTo(55.5f * scale + dx, 36f * scale + dy, 52.5f * scale + dx, 36f * scale + dy, 49.5f * scale + dx, 37.5f * scale + dy)
        cubicTo(46f * scale + dx, 39.2f * scale + dy, 44f * scale + dx, 42f * scale + dy, 44f * scale + dx, 45.5f * scale + dy)
        cubicTo(44f * scale + dx, 50f * scale + dy, 48f * scale + dx, 52.5f * scale + dy, 53.5f * scale + dx, 54f * scale + dy)
        cubicTo(58.5f * scale + dx, 55.5f * scale + dy, 62f * scale + dx, 57.5f * scale + dy, 62f * scale + dx, 61.5f * scale + dy)
        cubicTo(62f * scale + dx, 65.5f * scale + dy, 58.5f * scale + dx, 68.5f * scale + dy, 53.5f * scale + dx, 68.5f * scale + dy)
        cubicTo(48.5f * scale + dx, 68.5f * scale + dy, 44.5f * scale + dx, 66f * scale + dy, 42.5f * scale + dx, 63.5f * scale + dy)
        lineTo(39.5f * scale + dx, 67.5f * scale + dy)
        cubicTo(42.5f * scale + dx, 71f * scale + dy, 47.5f * scale + dx, 73.5f * scale + dy, 53.5f * scale + dx, 73.5f * scale + dy)
        cubicTo(61.5f * scale + dx, 73.5f * scale + dy, 67.5f * scale + dx, 69f * scale + dy, 67.5f * scale + dx, 61.5f * scale + dy)
        cubicTo(67.5f * scale + dx, 55.5f * scale + dy, 63f * scale + dx, 52.5f * scale + dy, 57f * scale + dx, 51f * scale + dy)
        cubicTo(52f * scale + dx, 49.5f * scale + dy, 49.5f * scale + dx, 48f * scale + dy, 49.5f * scale + dx, 45f * scale + dy)
        cubicTo(49.5f * scale + dx, 42.5f * scale + dy, 51.5f * scale + dx, 40.5f * scale + dy, 54.5f * scale + dx, 40.5f * scale + dy)
        cubicTo(57.5f * scale + dx, 40.5f * scale + dy, 60f * scale + dx, 41.8f * scale + dy, 62f * scale + dx, 44f * scale + dy)
        lineTo(65.5f * scale + dx, 40.5f * scale + dy)
        cubicTo(63.5f * scale + dx, 38f * scale + dy, 61f * scale + dx, 36.5f * scale + dy, 58.5f * scale + dx, 37.5f * scale + dy)
        close()
    }

    val sPaint = Paint().apply {
        isAntiAlias = true
        shader = LinearGradient(
            center.x - 16f * scale, center.y - 20f * scale,
            center.x + 16f * scale, center.y + 20f * scale,
            intArrayOf(
                android.graphics.Color.parseColor("#FFFDE7"),
                android.graphics.Color.parseColor("#FFE082"),
                android.graphics.Color.parseColor("#FFD54F"),
                android.graphics.Color.parseColor("#FFB300"),
                android.graphics.Color.parseColor("#FFA000"),
                android.graphics.Color.parseColor("#D4AF37"),
                android.graphics.Color.parseColor("#FFF9C4")
            ),
            floatArrayOf(0.0f, 0.18f, 0.38f, 0.60f, 0.78f, 0.90f, 1.0f),
            Shader.TileMode.CLAMP
        )
        setShadowLayer(scale * 4.0f, 0f, scale * 2.5f, android.graphics.Color.parseColor("#D0000000"))
    }
    canvas.drawPath(sPath, sPaint)

    val edgePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = scale * 0.8f
        color = android.graphics.Color.parseColor("#FFF9C4")
    }
    canvas.drawPath(sPath, edgePaint)

    return bitmap
}
