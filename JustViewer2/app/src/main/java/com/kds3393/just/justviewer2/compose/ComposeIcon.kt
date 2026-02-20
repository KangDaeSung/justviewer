package com.kds3393.just.justviewer2.compose

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CIcon(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String? = "",
    tint: Color = Colors.UnSet
) {
    Icon(painter = painter, tint = tint, contentDescription = contentDescription, modifier = modifier)
}

@Composable
fun CIcon(
    painter: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = "",
    tint: Color = Colors.UnSet
) {
    Icon(imageVector = painter, tint = tint, contentDescription = contentDescription, modifier = modifier)
}