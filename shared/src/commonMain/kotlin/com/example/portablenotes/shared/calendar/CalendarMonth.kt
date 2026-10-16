package com.example.portablenotes.shared.calendar

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class CalendarMonth(
    val year: Int,
    val month: Int
) {
    init {
        require(month in 1..12) { "month must be between 1 and 12" }
    }

    val days: List<LocalDate?>
        get() {
            val firstDay = LocalDate(year, month, 1)
            val dayCount = firstDay.daysUntil(firstDayOfNextMonth())
            val leadingDays = firstDay.dayOfWeek.isoDayNumber - 1
            val cellCount = ((leadingDays + dayCount + 6) / 7) * 7

            return List(cellCount) { index ->
                val dayOfMonth = index - leadingDays + 1
                if (dayOfMonth in 1..dayCount) {
                    LocalDate(year, month, dayOfMonth)
                } else {
                    null
                }
            }
        }

    fun next(): CalendarMonth =
        if (month == 12) of(year + 1, 1) else of(year, month + 1)

    fun previous(): CalendarMonth =
        if (month == 1) of(year - 1, 12) else of(year, month - 1)

    private fun firstDayOfNextMonth(): LocalDate =
        if (month == 12) LocalDate(year + 1, 1, 1)
        else LocalDate(year, month + 1, 1)

    companion object {
        fun of(year: Int, month: Int): CalendarMonth = CalendarMonth(year, month)

        @OptIn(ExperimentalTime::class)
        fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

        fun current(): CalendarMonth {
            val today = today()
            return of(today.year, today.month.ordinal + 1)
        }
    }
}