package de.iteratec.osm.api.dto

import de.iteratec.osm.csi.Page


class JsonPage {

    long id
    String name

    public static JsonPage create(Page page) {
        JsonPage result = new JsonPage()

        result.id = page.id
        result.name = page.name

        return result
    }

    public static Collection<JsonPage> create(Collection<Page> pages) {
        Set<JsonPage> result = []

        pages.each {
            result.add(create(it))
        }

        return result
    }
}
