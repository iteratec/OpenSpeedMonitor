package de.iteratec.osm.util

import org.grails.databinding.converters.ValueConverter
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.text.SimpleDateFormat

/**
 * A String to Date converter for format {@code dd.MM.yyyy}.
 * This is a migration of old de.iteratec.osm.util.CustomDateEditorRegistrar (in src/java/).
 * TODO: Do we really need this?
 * @author nkuhn
 */
class DateValueConverter implements ValueConverter{

    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy")

    boolean canConvert(value) {
        value instanceof String
    }
    def convert(value) {
        return SIMPLE_DATE_FORMAT.parse(value)
    }
    Class<?> getTargetType() {
        Date.class
    }
}
