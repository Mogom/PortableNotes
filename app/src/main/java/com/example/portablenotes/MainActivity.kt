package com.example.portablenotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.portablenotes.ui.theme.PortableNotesTheme
import com.example.portablenotes.ui.calendar.CalendarScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PortableNotesTheme {
                CalendarScreen()
            }
        }
    }
}