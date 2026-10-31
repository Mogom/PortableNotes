package com.example.portablenotes.shared.calendar

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CalendarMonthTest {
    @Test
    fun leapFebruaryHasTwentyNineDaysAndMondayFirstGrid() {
        val days = CalendarMonth.of(2024, 2).days

        assertEquals(35, days.size)
        assertNull(days[0])
        assertEquals(LocalDate(2024, 2, 1), days[3])
        assertEquals(29, days.filterNotNull().size)
    }

    @Test
    fun monthNavigationCrossesYearBoundaries() {
        assertEquals(CalendarMonth.of(2025, 1), CalendarMonth.of(2024, 12).next())
        assertEquals(CalendarMonth.of(2024, 12), CalendarMonth.of(2025, 1).previous())
    }
}