package com.portablenotes.domain.ui.navigation

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
            .background(com.portablenotes.domain.ui.AppColors.Background),
        shape = CircleShape,
        color = com.portablenotes.domain.ui.AppColors.Surface,
        shadowElevation = 14.dp,
        border = BorderStroke(1.dp, com.portablenotes.domain.ui.AppColors.Border),
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
    val background by animateColorAsState(
        if (isSelected) com.portablenotes.domain.ui.AppColors.Accent else androidx.compose.ui.graphics.Color.Transparent,
        animationSpec = tween(150),
    )
    val tint by animateColorAsState(
        if (isSelected) com.portablenotes.domain.ui.AppColors.OnAccent else com.portablenotes.domain.ui.AppColors.TextSecondary,
        animationSpec = tween(150),
    )

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .animateContentSize(tween(180)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(22.dp))
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(tween(150)) + expandHorizontally(tween(180)),
            exit = fadeOut(tween(100)) + shrinkHorizontally(tween(180)),
        ) {
            Row {
                Spacer(Modifier.width(8.dp))
                Text(tab.label, color = tint, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
    }
}