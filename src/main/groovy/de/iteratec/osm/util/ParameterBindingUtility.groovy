package de.iteratec.osm.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

class ParameterBindingUtility {
    
    private static final DateTimeFormatter FALLBACK_DATE_FORMAT = DateTimeFormat.forPattern('dd.MM.yyyy')
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime()

    static DateTime parseDateTimeParameter(def value, boolean fallbackToEndOfDay) {
        if (!value) {
            return null
        }
        if (value instanceof DateTime) {
            return value
        }
        String strValue = value.toString()
        try {
            return ISO_DATE_TIME_FORMATTER.parseDateTime(strValue)
        } catch (IllegalArgumentException ignored) {}
        DateTime fallback = FALLBACK_DATE_FORMAT.parseDateTime(strValue)
        return fallbackToEndOfDay ? fallback.millisOfDay().withMaximumValue() : fallback
    }

    static String formatDateTimeParameter(DateTime value) {
        return value ? ISO_DATE_TIME_FORMATTER.print(value) : null
    }
}
