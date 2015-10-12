package de.iteratec.osm.util

trait MessagePropertiesTrait {
    Properties getMessageKeys(Locale locale) {
        this.getMergedProperties(locale).properties
    }

    Properties getPluginMessageKeys(Locale locale) {
        this.getMergedPluginProperties(locale).properties
    }
}