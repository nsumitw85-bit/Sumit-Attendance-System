package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Official WhatsApp vector icon for Jetpack Compose.
 */
val WhatsAppVector: ImageVector by lazy {
    ImageVector.Builder(
        name = "WhatsApp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Outer chat bubble contour + phone handset icon
        path(
            fill = SolidColor(Color.White)
        ) {
            moveTo(12.04f, 2.0f)
            curveTo(6.58f, 2.0f, 2.13f, 6.45f, 2.13f, 11.91f)
            curveTo(2.13f, 13.66f, 2.59f, 15.36f, 3.45f, 16.86f)
            lineTo(2.05f, 22.0f)
            lineTo(7.3f, 20.62f)
            curveTo(8.75f, 21.41f, 10.38f, 21.83f, 12.04f, 21.83f)
            curveTo(17.5f, 21.83f, 21.95f, 17.38f, 21.95f, 11.92f)
            curveTo(21.95f, 6.46f, 17.5f, 2.0f, 12.04f, 2.0f)
            close()
            moveTo(17.84f, 16.14f)
            curveTo(17.6f, 16.82f, 16.65f, 17.43f, 15.84f, 17.61f)
            curveTo(15.29f, 17.73f, 14.58f, 17.82f, 12.16f, 16.82f)
            curveTo(9.07f, 15.54f, 7.07f, 12.4f, 6.92f, 12.2f)
            curveTo(6.77f, 12.0f, 5.69f, 10.57f, 5.69f, 9.09f)
            curveTo(5.69f, 7.61f, 6.44f, 6.89f, 6.74f, 6.58f)
            curveTo(6.99f, 6.32f, 7.4f, 6.2f, 7.79f, 6.2f)
            curveTo(7.92f, 6.2f, 8.04f, 6.21f, 8.15f, 6.22f)
            curveTo(8.46f, 6.23f, 8.61f, 6.25f, 8.81f, 6.73f)
            curveTo(9.07f, 7.36f, 9.69f, 8.88f, 9.77f, 9.04f)
            curveTo(9.85f, 9.2f, 9.92f, 9.42f, 9.82f, 9.62f)
            curveTo(9.71f, 9.83f, 9.63f, 9.93f, 9.48f, 10.1f)
            curveTo(9.33f, 10.27f, 9.17f, 10.47f, 9.04f, 10.61f)
            curveTo(8.89f, 10.77f, 8.73f, 10.94f, 8.9f, 11.24f)
            curveTo(9.07f, 11.53f, 9.66f, 12.49f, 10.52f, 13.26f)
            curveTo(11.64f, 14.25f, 12.55f, 14.57f, 12.87f, 14.71f)
            curveTo(13.12f, 14.81f, 13.41f, 14.79f, 13.59f, 14.6f)
            curveTo(13.82f, 14.35f, 14.1f, 13.95f, 14.39f, 13.54f)
            curveTo(14.6f, 13.24f, 14.86f, 13.28f, 15.14f, 13.38f)
            curveTo(15.43f, 13.49f, 16.96f, 14.25f, 17.27f, 14.4f)
            curveTo(17.58f, 14.56f, 17.78f, 14.63f, 17.86f, 14.76f)
            curveTo(17.93f, 14.89f, 17.93f, 15.52f, 17.84f, 16.14f)
            close()
        }
    }.build()
}

@Composable
fun WhatsAppIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 20.dp
) {
    Icon(
        imageVector = WhatsAppVector,
        contentDescription = "WhatsApp",
        tint = tint,
        modifier = modifier.size(size)
    )
}
