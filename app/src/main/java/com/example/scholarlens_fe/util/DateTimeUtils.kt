package com.example.scholarlens_fe.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date

/**
 * Date/time helper utilities.
 *
 * Supports parsing timestamps like "2024-12-01T00:00:00.000" (without timezone offset).
 */
object DateTimeUtils {

    // Pattern allows optional millisecond fraction
    private val ISO_LOCAL_WITH_OPTIONAL_MILLIS: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]")

    /**
     * Convert an ISO-like string (e.g. "2024-12-01T00:00:00.000") to LocalDateTime.
     * Returns null if parsing fails.
     */
    fun parseIsoToLocalDateTime(input: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(input, ISO_LOCAL_WITH_OPTIONAL_MILLIS)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /**
     * Convert the same string to Instant, assuming the value is in the given zone.
     * Useful when you need a point-in-time (epoch-based) value.
     */
    fun parseIsoToInstant(input: String, zoneId: ZoneId = ZoneId.systemDefault()): Instant? {
        val ldt = parseIsoToLocalDateTime(input) ?: return null
        return ldt.atZone(zoneId).toInstant()
    }

    /**
     * Convert the same string to legacy java.util.Date, assuming the value is in the given zone.
     */
    fun parseIsoToDate(input: String, zoneId: ZoneId = ZoneId.systemDefault()): Date? {
        val instant = parseIsoToInstant(input, zoneId) ?: return null
        return Date.from(instant)
    }
}


