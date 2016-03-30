package de.iteratec.osm.api.dto

import de.iteratec.osm.measurement.environment.Browser


class BrowserDto {

    long id
    String name

    public static BrowserDto create(Browser browser) {
        BrowserDto result = new BrowserDto()

        result.id = browser.id
        result.name = browser.name

        return result
    }

    public static Collection<BrowserDto> create(Collection<Browser> browsers) {
        Set<BrowserDto> result = []

        browsers.each {
            result.add(create(it))
        }

        return result
    }
}
