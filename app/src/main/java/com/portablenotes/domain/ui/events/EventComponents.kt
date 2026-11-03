package com.portablenotes.domain.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.models.events.Priority

/** Tres barritas: más llenas = más prioridad. Mantiene la paleta monocroma. */
@Composable
fun PriorityBars(priority: Priority, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        for (i in 1..3) {
            Box(
                Modifier
                    .width(4.dp)
                    .height((5 + i * 4).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i <= priority.level) _root_ide_package_.com.portablenotes.domain.ui.AppColors.Accent else _root_ide_package_.com.portablenotes.domain.ui.AppColors.Border)
            )
        }
    }
}

@Composable
fun SelectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) _root_ide_package_.com.portablenotes.domain.ui.AppColors.Accent else _root_ide_package_.com.portablenotes.domain.ui.AppColors.SurfaceMuted)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) _root_ide_package_.com.portablenotes.domain.ui.AppColors.OnAccent else _root_ide_package_.com.portablenotes.domain.ui.AppColors.TextPrimary,
        )
    }
}

@Composable
fun CircleIconButton(icon: ImageVector, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(_root_ide_package_.com.portablenotes.domain.ui.AppColors.SurfaceMuted)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = _root_ide_package_.com.portablenotes.domain.ui.AppColors.TextPrimary, modifier = Modifier.size(20.dp))
    }
}