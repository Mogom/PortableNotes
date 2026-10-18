package com.example.portablenotes.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portablenotes.ui.AppColors

data class DayEvent(
    val id: String,
    val title: String,
    val time: String,
    val place: String? = null,
    val tint: Color = AppColors.Tint1,
)

/** "Barra de notificaciones" con los eventos del día seleccionado. */
@Composable
fun TodayEventsPanel(
    events: List<DayEvent>,
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp, // espacio para la barra inferior flotante
) {
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Eventos de hoy",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(AppColors.Accent)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(events.size.toString(), color = AppColors.OnAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(12.dp))

        if (events.isEmpty()) {
            EmptyState(Modifier.fillMaxWidth().padding(top = 32.dp))
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = bottomPadding),
            ) {
                items(events, key = { it.id }) { EventCard(it) }
            }
        }
    }
}

@Composable
private fun EventCard(event: DayEvent) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = AppColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AppColors.Border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(event.tint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Notifications,
                    contentDescription = null,
                    tint = AppColors.TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    event.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                event.place?.let {
                    Text(it, fontSize = 12.sp, color = AppColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(AppColors.SurfaceMuted)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(event.time, fontSize = 12.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Todo despejado", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text("No tienes eventos programados", fontSize = 13.sp, color = AppColors.TextSecondary)
    }
}