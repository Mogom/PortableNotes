package com.example.portablenotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.portablenotes.ui.home.DayEvent
import com.example.portablenotes.ui.AppColors
import com.example.portablenotes.ui.home.HomeScreen
import com.example.portablenotes.ui.navigation.AppTab
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}

@Composable
fun App() {
    val today = remember { LocalDate.now() }
    var tab by remember { mutableStateOf(AppTab.Home) }

    MaterialTheme {
        HomeScreen(
            today = today,
            selectedTab = tab,
            onTabSelected = { tab = it },
            eventsFor = { date ->
                // Reemplaza por tu ViewModel / repositorio (Room, API, etc.)
                if (date.dayOfMonth % 2 == 0) emptyList() else listOf(
                    DayEvent("1", "Reunión de equipo", "09:00", "Sala 3", AppColors.Tint1),
                    DayEvent("2", "Entrenamiento", "13:30", "Gimnasio", AppColors.Tint2),
                    DayEvent("3", "Llamada con cliente", "16:00", "Meet", AppColors.Tint3),
                    DayEvent("4", "Revisar rutina nocturna", "21:00", null, AppColors.Tint1),
                )
            },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomePreview() = App()