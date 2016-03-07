package de.iteratec.osm.api.dto

import de.iteratec.osm.measurement.environment.Browser


class JsonBrowser {

    long id
    String name

    public static JsonBrowser create(Browser browser) {
        JsonBrowser result = new JsonBrowser()

        result.id = browser.id
        result.name = browser.name

        return result
    }

    public static Collection<JsonBrowser> create(Collection<Browser> browsers) {
        Set<JsonBrowser> result = []

        browsers.each {
            result.add(create(it))
        }

        return result
    }
}
