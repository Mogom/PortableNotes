package com.portablenotes.domain.ui.events

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.portablenotes.domain.models.events.EventType

/** Ícono alusivo a la categoría del evento. Se usa en Inicio, Eventos, detalle y editor. */
val EventType.icon: ImageVector
    get() = when (this) {
        EventType.Work -> Icons.Rounded.Work
        EventType.Leisure -> Icons.Rounded.SportsEsports
        EventType.Commitment -> Icons.Rounded.AssignmentTurnedIn
        EventType.Relationships -> Icons.Rounded.Favorite
        EventType.Family -> Icons.Rounded.FamilyRestroom
        EventType.Health -> Icons.Rounded.HealthAndSafety
        EventType.Other -> Icons.Rounded.Star
    }
