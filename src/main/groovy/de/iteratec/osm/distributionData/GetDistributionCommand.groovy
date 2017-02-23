package de.iteratec.osm.distributionData

import grails.databinding.BindUsing
import grails.validation.Validateable
import groovy.json.JsonSlurper
import org.joda.time.DateTime

class GetDistributionCommand implements Validateable {
    DateTime from
    DateTime to

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['selectedPages'])
    })
    List<String> selectedPages

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['selectedJobGroups'])
    })
    List<String> selectedJobGroups

//    @BindUsing({ obj, source ->
//        return new JsonSlurper().parseText(source['selectedSeries'])
//    })
//    List selectedSeries
}
