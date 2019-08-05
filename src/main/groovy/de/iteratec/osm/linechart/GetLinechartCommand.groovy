package de.iteratec.osm.linechart

import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.SelectedMeasurandType
import de.iteratec.osm.result.TimeSeriesShowCommandBase
import grails.databinding.BindUsing
import grails.validation.Validateable
import groovy.json.JsonSlurper
import org.joda.time.DateTime

class GetLinechartCommand extends TimeSeriesShowCommandBase {
    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['preconfiguredDashboard'])
    })
    List<Long> preconfiguredDashboard = []

    DateTime from
    DateTime to

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['interval'])
    })
    List<Long> interval = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['measurands'])
    })
    List<String> measurands = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['jobGroups'])
    })
    List<Long> jobGroups = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['pages'])
    })
    List<Long> pages = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['measuredEvents'])
    })
    List<Long> measuredEvents = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['browsers'])
    })
    List<Long> browsers = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['locations'])
    })
    List<Long> locations = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['connectivities'])
    })
    List<Long> connectivities = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['deviceTypes'])
    })
    List<String> deviceTypes = []

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['operatingSystems'])
    })
    List<String> operatingSystems = []
}