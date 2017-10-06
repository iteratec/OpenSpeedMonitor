package de.iteratec.osm.barchart

import grails.databinding.BindUsing
import grails.validation.Validateable
import groovy.json.JsonSlurper
import org.joda.time.DateTime

class GetBarchartCommand implements Validateable {
    DateTime from
    DateTime to

    DateTime fromComparative
    DateTime toComparative

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

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['selectedAggregationValue'])
    })
    String selectedAggregationValue
}
