package com.example.scholarlens_fe.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

/**
 * Date/time helper utilities.
 *
 * Supports parsing timestamps like "2024-12-01T00:00:00.000" (without timezone offset).
 */
object DateTimeUtils {

    // Pattern allows optional millisecond fraction
    private val ISO_LOCAL_WITH_OPTIONAL_MILLIS: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]")

    // Format for birth date: "September 30, 2005 at 07:00:00 UTC+7"
    // Note: Using custom formatter to handle UTC+7 format (without :00)
    private val BIRTH_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(
        "MMMM d, yyyy 'at' HH:mm:ss 'UTC'X",
        Locale.ENGLISH
    )

    // Format for parsing birth date from backend
    // Try multiple patterns to handle variations (UTC+7, UTC+07:00, etc.)
    private val BIRTH_DATE_PARSER_PATTERNS = listOf(
        DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm:ss 'UTC'X", Locale.ENGLISH), // UTC+7
        DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm:ss 'UTC'XXX", Locale.ENGLISH), // UTC+07:00
        DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm:ss 'UTC'+07:00", Locale.ENGLISH) // Explicit +07:00
    )

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

    /**
     * Format a LocalDate to birth date format: "September 30, 2005 at 07:00:00 UTC+7"
     * Uses UTC+7 timezone (Asia/Ho_Chi_Minh)
     */
    fun formatBirthDate(date: LocalDate): String {
        val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
        val zonedDateTime = date.atTime(7, 0, 0).atZone(zoneId)
        return zonedDateTime.format(BIRTH_DATE_FORMATTER)
    }

    /**
     * Parse birth date from format: "September 30, 2005 at 07:00:00 UTC+7"
     * Returns LocalDate or null if parsing fails
     * Tries multiple patterns to handle variations
     */
    fun parseBirthDate(input: String): LocalDate? {
        if (input.isBlank()) return null
        
        for (pattern in BIRTH_DATE_PARSER_PATTERNS) {
            try {
                val zonedDateTime = ZonedDateTime.parse(input, pattern)
                return zonedDateTime.toLocalDate()
            } catch (e: DateTimeParseException) {
                // Try next pattern
                continue
            }
        }
        
        // If all patterns fail, return null
        return null
    }

    /**
     * Format LocalDate to display format (e.g., "September 30, 2005")
     */
    fun formatDateForDisplay(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
        return date.format(formatter)
    }
}


