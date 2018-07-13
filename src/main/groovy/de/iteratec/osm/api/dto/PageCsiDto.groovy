package de.iteratec.osm.api.dto

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = "id")
class PageCsiDto {
    Long id
    String date
    double csiDocComplete
    double csiVisComplete
}
