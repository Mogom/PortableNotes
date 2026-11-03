package com.portablenotes.domain.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portablenotes.domain.ui.AppColors
import com.portablenotes.domain.ui.events.SelectChip
import com.portablenotes.notifications.EventAlarmScheduler
import com.portablenotes.notifications.MotivationScheduler
import com.portablenotes.notifications.NotificationPrefs

/** Ajustes de notificaciones: antelación del recordatorio y mensajes motivadores. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationSettingsSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var lead by remember { mutableStateOf(NotificationPrefs.leadMinutes(context)) }
    var motivation by remember { mutableStateOf(NotificationPrefs.motivationEnabled(context)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Notificaciones", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Antelación del recordatorio", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                Text(
                    "Cuánto antes te avisamos de que un evento va a empezar.",
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary,
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NotificationPrefs.LEAD_OPTIONS.forEach { minutes ->
                    SelectChip(NotificationPrefs.leadLabel(minutes), minutes == lead) {
                        lead = minutes
                        NotificationPrefs.setLeadMinutes(context, minutes)
                        EventAlarmScheduler.rescheduleAll(context)   // aplica el cambio a los eventos ya creados
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Mensajes motivadores", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                    Text("Dos al día, uno por la mañana y otro por la tarde.", fontSize = 13.sp, color = AppColors.TextSecondary)
                }
                Switch(
                    checked = motivation,
                    onCheckedChange = {
                        motivation = it
                        NotificationPrefs.setMotivationEnabled(context, it)
                        MotivationScheduler.sync(context)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.OnAccent,
                        checkedTrackColor = AppColors.Accent,
                        uncheckedThumbColor = AppColors.TextSecondary,
                        uncheckedTrackColor = AppColors.SurfaceMuted,
                        uncheckedBorderColor = AppColors.Border,
                    ),
                )
            }
        }
    }
}
