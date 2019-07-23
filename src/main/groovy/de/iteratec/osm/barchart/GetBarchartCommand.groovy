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
        return new JsonSlurper().parseText(source['pages'])
    })
    List<Long> pages = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['jobGroups'])
    })
    List<Long> jobGroups = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['measurands'])
    })
    List<String> measurands = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['browsers'])
    })
    List<Long> browsers = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['deviceTypes'])
    })
    List<String> deviceTypes = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['operatingSystems'])
    })
    List<String> operatingSystems = []

    String aggregationValue
}
