package de.iteratec.osm.linechart

import grails.databinding.BindUsing
import grails.validation.Validateable
import groovy.json.JsonSlurper
import org.joda.time.DateTime

class GetLinechartCommand implements Validateable {
    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['preconfiguredDashboard'])
    })
    List<Long> preconfiguredDashboard = []

    DateTime from
    DateTime to

    @BindUsing({ obj, source ->
        return new JsonSlurper().parseText(source['interval'])
    })
    Long interval = 0

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

    static constraints = {
        preconfiguredDashboard(nullable: true)
        from(nullable: true, validator: { DateTime from, GetLinechartCommand cmd ->
            if (from == null && cmd.interval <= 0) {
                return false
            }
        })
        to(nullable: true, validator: { DateTime to, GetLinechartCommand cmd ->
            if (to == null && cmd.interval <= 0) {
                return ['de.iteratec.osm.gui.startAndEndDateSelection.error.to.nullWithManualSelection']
            } else if (cmd.from != null && cmd.to != null && cmd.interval == 0 && !cmd.to.isAfter(cmd.from)) {
                return false
            }
        })
        interval(nullable: true)
        measurands(nullable: false, validator: { List<String> measurands, GetLinechartCommand cmd ->
            if (measurands.isEmpty()) {
                return false
            }
        })
        jobGroups(nullable: false, validator: { List<Long> jobGroups, GetLinechartCommand cmd ->
            if (jobGroups.isEmpty()) {
                return false
            }
        })
        pages(nullable: true, validator: { List<Long> pages, GetLinechartCommand cmd ->
            if (!pages && !cmd.measuredEvents) {
                return false
            }
        })
        measuredEvents(nullable: true)
        browsers(nullable: true)
        locations(nullable: true)
        connectivities(nullable: true)
        deviceTypes(nullable: true)
        operatingSystems(nullable: true)
    }
}