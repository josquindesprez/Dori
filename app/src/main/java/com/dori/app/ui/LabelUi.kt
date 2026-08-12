package com.dori.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dori.app.data.Label

@Composable
fun LabelDot(colorArgb: Int, modifier: Modifier = Modifier, dotSize: Dp = 10.dp) {
    Box(
        modifier = modifier
            .size(dotSize)
            .background(Color(colorArgb), CircleShape)
    )
}

@Composable
fun LabelChip(label: Label, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        LabelDot(label.colorArgb)
        Text(
            text = label.name,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}
