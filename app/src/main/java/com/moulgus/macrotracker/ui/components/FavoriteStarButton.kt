package com.moulgus.macrotracker.ui.components

import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun FavoriteStarButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Text(
            text = if (isFavorite) "★" else "☆",
            color = if (isFavorite) {
                Color(0xFFFFD54F)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontSize = 26.sp
        )
    }
}