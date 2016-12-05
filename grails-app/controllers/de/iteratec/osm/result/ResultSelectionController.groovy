package de.iteratec.osm.result

import de.iteratec.osm.api.dto.JobGroupDto
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.util.ControllerUtils
import org.joda.time.DateTime
import org.springframework.http.HttpStatus

class ResultSelectionController {
    JobGroupDaoService jobGroupDaoService

    enum MetaConnectivityProfileId {
        Custom(-2), Native(1)

        MetaConnectivityProfileId(int value) {
            this.value = value
        }
        int value
    }

    enum ResultSelectionType {
        JobGroups,
        MeasuredEvents,
        Locations,
        ConnectivityProfiles
    }

    Closure resultSelectionFilters = { command, resultSelectionType ->
        and {
            between("jobResultDate", command.from.toDate(), command.to.toDate())
            if (resultSelectionType != ResultSelectionType.JobGroups && command.jobGroupIds) {
                jobGroup {
                    'in'("id", command.jobGroupIds)
                }
            }

            if (resultSelectionType != ResultSelectionType.MeasuredEvents && command.measuredEventIds) {
                measuredEvent {
                    'in'("id", command.measuredEventIds)
                }
            } else if (resultSelectionType != ResultSelectionType.MeasuredEvents && command.pageIds) {
                page {
                    'in'("id", command.pageIds)
                }
            }

            if (resultSelectionType != ResultSelectionType.Locations && command.locationIds) {
                location {
                    'in'("id", command.locationIds)
                }
            } else if (resultSelectionType != ResultSelectionType.Locations && command.browserIds) {
                browser {
                    'in'("id", command.browserIds)
                }
            }
        }
    }

    def getJobGroups(ResultSelectionCommand command) {
        if (command.hasErrors()) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST,
                    "Invalid parameters: " + command.getErrors().fieldErrors.each{it.field}.join(", "))
            return
        }
        if (!command.from.isBefore(command.to)) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST,
                    "Invalid time frame: 'from' value needs to be before 'to'")
            return
        }
        def start = DateTime.now().getMillis()
        def availableJobGroups = ResultSelectionInformation.createCriteria().list {

            resultSelectionFilters.delegate = delegate
            resultSelectionFilters(command, ResultSelectionType.JobGroups)

            projections {
                distinct('jobGroup')
            }
        }
        println "Query took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        def jobGroupDtos = availableJobGroups.collect { ([id: it.id, name: it.name] as JobGroupDto) }
        println "Total took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        ControllerUtils.sendObjectAsJSON(response, jobGroupDtos)
    }

    def getMeasuredEvents(ResultSelectionCommand command) {
        // need to explicitly select id an name, since gorm/hibernate takes 10x as long for fetching the page
        def start = DateTime.now().getMillis()
        def measuredEvents = ResultSelectionInformation.createCriteria().list {
            resultSelectionFilters.delegate = delegate
            resultSelectionFilters(command, ResultSelectionType.MeasuredEvents)

            projections {
                distinct('measuredEvent')
                property('page')
            }
        }
        println "Query took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        def measuredEventDtos = measuredEvents.collect {[
                id: it[0].id,
                name: it[0].name,
                parent: [id: it[1].id, name: it[1].name]
        ]}
        println "Total took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        ControllerUtils.sendObjectAsJSON(response, measuredEventDtos)
    }

    def getLocations(ResultSelectionCommand command) {
        // need to explicitly select id an name, since gorm/hibernate takes 10x as long for fetching the page
        def start = DateTime.now().getMillis()
        def measuredEvents = ResultSelectionInformation.createCriteria().list {
            resultSelectionFilters.delegate = delegate
            resultSelectionFilters(command, ResultSelectionType.Locations)

            projections {
                distinct('location')
                property('browser')
            }
        }
        println "Query took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        def measuredEventDtos = measuredEvents.collect { [
                id: it[0].id,
                name: it[0].toString(),
                parent: [id: it[1].id, name: it[1].name]
        ] }
        println "Total took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        ControllerUtils.sendObjectAsJSON(response, measuredEventDtos)
    }

    def getConnectivityProfiles(ResultSelectionCommand command) {
        // need to explicitly select id an name, since gorm/hibernate takes 10x as long for fetching the page
        def totalStart = DateTime.now().getMillis()
        def start = totalStart
        def connectivityProfiles = ResultSelectionInformation.createCriteria().list {
            resultSelectionFilters.delegate = delegate
            resultSelectionFilters(command, ResultSelectionType.ConnectivityProfiles)

            projections {
                distinct('connectivityProfile')
            }
        }
        println "Query took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"

        def dtos = connectivityProfiles.collect { [
                id: it.id,
                name: it.toString()
        ] }
        println "Total took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        start = DateTime.now().getMillis()

        def customProfiles = ResultSelectionInformation.createCriteria().list {
            resultSelectionFilters.delegate = delegate
            resultSelectionFilters(command, ResultSelectionType.ConnectivityProfiles)
            isNotNull('customConnectivityName')

            projections {
                distinct('customConnectivityName')
            }
        }
        println "Query took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        dtos.addAll(customProfiles.collect {[id:MetaConnectivityProfileId.Custom.value, name: it]})
        println "Total took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        start = DateTime.now().getMillis()

        def nativeConnectivity = ResultSelectionInformation.createCriteria().list(max: 1) {
            resultSelectionFilters.delegate = delegate
            resultSelectionFilters(command, ResultSelectionType.ConnectivityProfiles)
            eq('noTrafficShapingAtAll', true)

            projections {
                property('id')
            }
        }
        println "Query took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        if (nativeConnectivity) {
            dtos.add([id: MetaConnectivityProfileId.Native.value, name: "Native"])
        }
        println "Total took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"

        println "All total took " + ((DateTime.now().getMillis() - totalStart) / 1000) + " seconds"
        ControllerUtils.sendObjectAsJSON(response, dtos)
    }
}

class ResultSelectionCommand {
    DateTime from
    DateTime to
    List<Long> jobGroupIds
    List<Long> pageIds
    List<Long> measuredEventIds
    List<Long> browserIds
    List<Long> locationIds
}
