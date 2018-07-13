package de.iteratec.osm.api.dto

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = "pageId")
class PageCsiDto {
    Long pageId
    String date
    double csiDocComplete
    double csiVisComplete
}
