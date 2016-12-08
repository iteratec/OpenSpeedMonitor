package de.iteratec.osm.dimple

import grails.databinding.BindUsing
import grails.validation.Validateable
import groovy.json.JsonSlurper

class GetBarchartCommand implements Validateable {
    Date from
    Date to

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['selectedPages'])
    })
    List<String> selectedPages

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['selectedJobGroups'])
    })
    List<String> selectedJobGroups

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['selectedSeries'])
    })
    List selectedSeries
}
