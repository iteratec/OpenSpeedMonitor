package de.iteratec.osm.util

import org.grails.databinding.converters.ValueConverter
import org.springframework.util.NumberUtils

import java.text.DecimalFormat

/**
 * A String to Double converter. 
 * Without this Double values got multiplicated by 10 with each save() on crud edit view.
 * @author nkuhn
 */
class DoubleValueConverter implements ValueConverter{
    boolean canConvert(value) {
        value instanceof String
    }
    def convert(value) {
        return NumberUtils.parseNumber(value, Double.class, DecimalFormat.getInstance(Locale.ENGLISH))
    }
    Class<?> getTargetType() {
        Double.class
    }
}
