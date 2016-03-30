package de.iteratec.osm.api.dto

import de.iteratec.osm.csi.Page


class PageDto {

    long id
    String name

    public static PageDto create(Page page) {
        PageDto result = new PageDto()

        result.id = page.id
        result.name = page.name

        return result
    }

    public static Collection<PageDto> create(Collection<Page> pages) {
        Set<PageDto> result = []

        pages.each {
            result.add(create(it))
        }

        return result
    }
}
