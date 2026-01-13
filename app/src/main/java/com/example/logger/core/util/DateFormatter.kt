package com.example.logger.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Centralized date formatting utility using IST (Indian Standard Time) timezone.
 * All date/time operations should use these formatters to ensure consistency across the app.
 */
object DateFormatter {
    private const val IST_TIMEZONE = "Asia/Kolkata"

    /**
     * Get TimeZone for IST
     */
    val istTimeZone: TimeZone
        get() = TimeZone.getTimeZone(IST_TIMEZONE)

    /**
     * Format: yyyy-MM-dd (e.g., 2026-01-07)
     * Used for API calls and date keys
     */
    fun getApiDateFormat(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = istTimeZone
    }

    /**
     * Format: dd-MM-yyyy (e.g., 07-01-2026)
     * Used for input date display
     */
    fun getInputDateFormat(): SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).apply {
        timeZone = istTimeZone
    }

    /**
     * Format: dd MMM yyyy (e.g., 07 Jan 2026)
     * Used for display purposes
     */
    fun getDisplayDateFormat(): SimpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
        timeZone = istTimeZone
    }

    /**
     * Format: yyyyMMdd (e.g., 20260107)
     * Used for date comparisons
     */
    fun getCompactDateFormat(): SimpleDateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).apply {
        timeZone = istTimeZone
    }

    /**
     * Format: HH:mm (e.g., 14:30)
     * Used for time display
     */
    fun getTimeFormat(): SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = istTimeZone
    }

    /**
     * Format: yyyy-MM-dd'T'HH:mm:ss (e.g., 2026-01-07T14:30:00)
     * Used for parsing datetime strings from API
     */
    fun getDateTimeFormat(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = istTimeZone
    }

    /**
     * Format: yyyy-MM-dd HH:mm:ss (e.g., 2026-01-07 14:30:00)
     * Alternative datetime format for parsing API responses
     */
    fun getAlternativeDateTimeFormat(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = istTimeZone
    }

    /**
     * Get current date in IST
     */
    fun getCurrentDate(): Date = Date()

    /**
     * Format current date to API format (yyyy-MM-dd) in IST
     */
    fun getCurrentDateString(): String = getApiDateFormat().format(getCurrentDate())

    /**
     * Format current time to HH:mm in IST
     */
    fun getCurrentTimeString(): String = getTimeFormat().format(getCurrentDate())

    /**
     * Parse datetime string to time string (HH:mm)
     * Supports multiple datetime formats from API
     * Converts from UTC/GMT to IST timezone
     */
    fun parseToTimeString(dateTimeString: String?): String? {
        if (dateTimeString.isNullOrBlank()) return null

        return try {
            // API sends time in UTC/GMT timezone, we need to parse with UTC and convert to IST
            val utcFormat = try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            } catch (e: Exception) {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            }

            // Parse the datetime string as UTC
            val date = try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(dateTimeString)
            } catch (e: Exception) {
                // Try alternative format (yyyy-MM-dd HH:mm:ss)
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(dateTimeString)
            }

            // Format the date in IST timezone
            date?.let { getTimeFormat().format(it) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse date string to Date object
     */
    fun parseApiDate(dateString: String): Date? = try {
        getApiDateFormat().parse(dateString)
    } catch (e: Exception) {
        null
    }

    /**
     * Get date string for N days ago in API format (yyyy-MM-dd) in IST
     */
    fun getDateDaysAgoString(daysAgo: Int): String {
        val cal = Calendar.getInstance(istTimeZone)
        cal.time = getCurrentDate()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return getApiDateFormat().format(cal.time)
    }
}
