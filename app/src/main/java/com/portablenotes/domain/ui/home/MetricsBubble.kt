package com.portablenotes.domain.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.AppColors
import kotlin.math.roundToInt

private val MissedColor = Color(0xFFFF6B6B)

/**
 * Burbuja de progreso. Cerrada: anillo con el porcentaje y "X de Y cumplidas".
 * Al tocarla se despliega el detalle de tareas y de eventos (cumplidos / pendientes / no cumplidos).
 */
@Composable
fun MetricsBubble(metrics: HomeMetrics, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val arrow by animateFloatAsState(if (expanded) 180f else 0f, tween(200), label = "arrow")
    val progress by animateFloatAsState(metrics.progress, tween(400), label = "progress")

    Surface(
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
        color = AppColors.Surface,
        border = BorderStroke(1.dp, AppColors.Border),
        modifier = modifier.fillMaxWidth().animateContentSize(tween(220)),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(46.dp),
                        color = AppColors.Accent,
                        trackColor = AppColors.SurfaceMuted,
                        strokeWidth = 4.dp,
                    )
                    Text(
                        "${(metrics.progress * 100).roundToInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Tu progreso", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                    Text(
                        if (metrics.total == 0) "Aún no hay nada que medir"
                        else "${metrics.done} de ${metrics.total} cumplidas",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary,
                    )
                }
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (expanded) "Ocultar métricas" else "Ver métricas",
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.rotate(arrow),
                )
            }

            if (expanded) {
                Spacer(Modifier.height(14.dp))
                MetricSection(
                    title = "Tareas",
                    done = metrics.tasksDone,
                    pending = metrics.tasksPending,
                    missed = 0,
                    emptyText = "No tienes tareas todavía",
                )
                Spacer(Modifier.height(14.dp))
                MetricSection(
                    title = "Eventos",
                    done = metrics.eventsDone,
                    pending = metrics.eventsPending,
                    missed = metrics.eventsMissed,
                    emptyText = "No tienes eventos únicos todavía",
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Los eventos que se repiten no cuentan porque no se marcan como hechos.",
                    fontSize = 11.sp,
                    color = AppColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun MetricSection(title: String, done: Int, pending: Int, missed: Int, emptyText: String) {
    val total = done + pending + missed
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
            if (total > 0) Text("$done/$total", fontSize = 12.sp, color = AppColors.TextSecondary)
        }
        if (total == 0) {
            Text(emptyText, fontSize = 12.sp, color = AppColors.TextSecondary)
        } else {
            // Barra apilada: cumplidas | pendientes | no cumplidas
            Row(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(AppColors.SurfaceMuted)) {
                if (done > 0) Box(Modifier.weight(done.toFloat()).height(8.dp).background(AppColors.Accent))
                if (pending > 0) Box(Modifier.weight(pending.toFloat()).height(8.dp).background(AppColors.Border))
                if (missed > 0) Box(Modifier.weight(missed.toFloat()).height(8.dp).background(MissedColor))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Legend(AppColors.Accent, "$done cumplidas")
                Legend(AppColors.Border, "$pending pendientes")
                if (missed > 0) Legend(MissedColor, "$missed no cumplidas")
            }
        }
    }
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(text, fontSize = 12.sp, color = AppColors.TextSecondary)
    }
}
