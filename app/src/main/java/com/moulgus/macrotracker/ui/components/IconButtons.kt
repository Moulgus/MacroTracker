package com.moulgus.macrotracker.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

@Composable
fun HeaderIconButton(
    @DrawableRes iconResID: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(42.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResID),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SmallActionIconButton(
    @DrawableRes iconResID: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    enabled: Boolean = true,
    buttonSize: Dp = 40.dp,
    iconSize: Dp = 22.dp
) {
    val finalTint = if (enabled) {
        tint
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    }

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(buttonSize)
    ) {
        Icon(
            painter = painterResource(id = iconResID),
            contentDescription = contentDescription,
            tint = finalTint,
            modifier = Modifier.size(iconSize)
        )
    }
}