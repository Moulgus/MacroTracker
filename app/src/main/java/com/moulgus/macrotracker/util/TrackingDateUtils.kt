package com.moulgus.macrotracker.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TrackingDateUtils {

    private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dayStartTime: LocalTime = LocalTime.of(4, 0)

    fun getCurrentTrackingDate(): LocalDate {
        val now = LocalDateTime.now()

        return if (now.toLocalTime().isBefore(dayStartTime)) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
    }

    fun getCurrentTrackingDateString(): String {
        return getCurrentTrackingDate().format(isoFormatter)
    }
}