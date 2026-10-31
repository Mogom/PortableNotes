package com.portablenotes.domain.ui.navigation

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.AppColors

/** Barra de navegación flotante tipo píldora. Componente independiente y reutilizable. */
@Composable
fun AppBottomBar(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(_root_ide_package_.com.portablenotes.domain.ui.AppColors.Background),
        shape = CircleShape,
        color = _root_ide_package_.com.portablenotes.domain.ui.AppColors.Surface,
        shadowElevation = 14.dp,
        border = BorderStroke(1.dp, _root_ide_package_.com.portablenotes.domain.ui.AppColors.Border),
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp,8.dp,8.dp,16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            AppTab.entries.forEach { tab ->
                BarItem(tab = tab, isSelected = tab == selected, onClick = { onSelect(tab) })
            }
        }
    }
}

@Composable
private fun BarItem(tab: AppTab, isSelected: Boolean, onClick: () -> Unit) {
    val background by animateColorAsState(if (isSelected) _root_ide_package_.com.portablenotes.domain.ui.AppColors.Accent else androidx.compose.ui.graphics.Color.Transparent)
    val tint by animateColorAsState(if (isSelected) _root_ide_package_.com.portablenotes.domain.ui.AppColors.OnAccent else _root_ide_package_.com.portablenotes.domain.ui.AppColors.TextSecondary)

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(22.dp))
        AnimatedVisibility(visible = isSelected) {
            Row {
                Spacer(Modifier.width(8.dp))
                Text(tab.label, color = tint, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
    }
}