package de.iteratec.osm.util

import grails.databinding.converters.ValueConverter

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
        if (value) {
            String strDate = SIMPLE_DATE_FORMAT.format(value);
            Date parsedDate = SIMPLE_DATE_FORMAT.parse(strDate);
            return parsedDate
        } else {
            return null;
        }
    }
    Class<?> getTargetType() {
        Date.class
    }
    public static getConverter(){
        return new DateValueConverter()
    }
}
