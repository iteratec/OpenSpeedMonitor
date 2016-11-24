package de.iteratec.osm.result

import de.iteratec.osm.api.dto.JobGroupDto
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.util.ControllerUtils
import org.hibernate.FetchMode
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
        if (!command.measuredEventIds && !command.pageIds && !command.browserIds && !command.locationIds) {
            def availableJobGroups = jobGroupDaoService.findByJobResultsInTimeFrame(command.from.toDate(), command.to.toDate())
            ControllerUtils.sendObjectAsJSON(response, JobGroupDto.create(availableJobGroups))
            return
        }

        def start = DateTime.now().getMillis()
        def availableJobGroups = EventResult.createCriteria().list {
            fetchMode('page', FetchMode.JOIN)
            fetchMode('measuredEvent', FetchMode.JOIN)
            fetchMode('jobGroup', FetchMode.JOIN)

            and {
                between("jobResultDate", command.from.toDate(), command.to.toDate())

                if (command.measuredEventIds) {
                    measuredEvent {
                        'in'("id", command.measuredEventIds)
                    }
                } else if (command.pageIds) {
                    page {
                        'in'("id", command.pageIds)
                    }
                }

                if (command.locationIds) {
                    location {
                        'in'("id", command.locationIds)
                    }
                } else if (command.browserIds) {
                    browser {
                        'in'("id", command.browserIds)
                    }
                }
            }

            projections {
                jobGroup {
                    distinct('id')
                    property('name')
                }
            }
        }
        println "Took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        def jobGroupDtos = availableJobGroups.collect { ([id: it[0], name: it[1]] as JobGroupDto) }
        ControllerUtils.sendObjectAsJSON(response, jobGroupDtos)
    }

    def getMeasuredEvents(ResultSelectionCommand command) {
        // need to explicitly select id an name, since gorm/hibernate takes 10x as long for fetching the page
        def start = DateTime.now().getMillis()
        def measuredEvents = EventResult.createCriteria().list {
            fetchMode('page', FetchMode.JOIN)
            fetchMode('measuredEvent', FetchMode.JOIN)
            and {
                between("jobResultDate", command.from.toDate(), command.to.toDate())
                if (command.jobGroupIds) {
                    jobGroup {
                        'in'("id", command.jobGroupIds)
                    }
                }

                if (command.locationIds) {
                    location {
                        'in'("id", command.locationIds)
                    }
                } else if (command.browserIds) {
                    browser {
                        'in'("id", command.browserIds)
                    }
                }
            }

            projections {
                measuredEvent {
                    distinct('id')
                    property('name')
                }
                page {
                    property('id')
                    property('name')
                }
            }
        }
        def measuredEventDtos = measuredEvents.collect {[
                id: it[0],
                name: it[1],
                parent: [id: it[2], name: it[3]]
        ]}
        println "Took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        ControllerUtils.sendObjectAsJSON(response, measuredEventDtos)
    }

    def getLocations(ResultSelectionCommand command) {
        // need to explicitly select id an name, since gorm/hibernate takes 10x as long for fetching the page
        def start = DateTime.now().getMillis()
        def measuredEvents = EventResult.createCriteria().list {
            fetchMode('location', FetchMode.JOIN)
            fetchMode('browser', FetchMode.JOIN)
            and {
                between("jobResultDate", command.from.toDate(), command.to.toDate())
                if (command.jobGroupIds) {
                    jobGroup {
                        'in'("id", command.jobGroupIds)
                    }
                }
                if (command.measuredEventIds) {
                    measuredEvent {
                        'in'("id", command.measuredEventIds)
                    }
                } else if (command.pageIds) {
                    page {
                        'in'("id", command.pageIds)
                    }
                }
                if (command.locationIds) {
                    location {
                        'in'("id", command.locationIds)
                    }
                } else if (command.browserIds) {
                    browser {
                        'in'("id", command.browserIds)
                    }
                }

            }

            projections {
                location {
                    distinct('id')
                    property('label')

                    wptServer {
                        property('label')
                    }
                }
                browser {
                    property('id')
                    property('name')
                }
            }
        }
        def measuredEventDtos = measuredEvents.collect { [
                id: it[0],
                name: it[1] + " @ " + it[2] + " (" + it[4] + ")",
                parent: [id: it[3], name: it[4]]
        ] }
        println "Took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        ControllerUtils.sendObjectAsJSON(response, measuredEventDtos)
    }

    def getConnectivityProfiles(ResultSelectionCommand command) {
        // need to explicitly select id an name, since gorm/hibernate takes 10x as long for fetching the page
        def start = DateTime.now().getMillis()
        def connectivityProfiles = EventResult.createCriteria().list {
            fetchMode('connectivityProfile', FetchMode.JOIN)
            and {
                between("jobResultDate", command.from.toDate(), command.to.toDate())
                if (command.jobGroupIds) {
                    jobGroup {
                        'in'("id", command.jobGroupIds)
                    }
                }
                if (command.measuredEventIds) {
                    measuredEvent {
                        'in'("id", command.measuredEventIds)
                    }
                } else if (command.pageIds) {
                    page {
                        'in'("id", command.pageIds)
                    }
                }
                if (command.locationIds) {
                    location {
                        'in'("id", command.locationIds)
                    }
                } else if (command.browserIds) {
                    browser {
                        'in'("id", command.browserIds)
                    }
                }
            }

            projections {
                connectivityProfile {
                    distinct('id')
                    property('name')
                    property('bandwidthDown')
                    property('bandwidthUp')
                    property('latency')
                    property('packetLoss')
                }
            }
        }

        def dtos = connectivityProfiles.collect { [
                id: it[0],
                name: "${it[1]}: ${it[2]}/${it[3]} Kbps, ${it[4]}ms Latency" + (it[5] ? "${it[5]}% PLR" : "")
        ] }

        def customProfiles = EventResult.createCriteria().list {
            and {
                between("jobResultDate", command.from.toDate(), command.to.toDate())
                if (command.jobGroupIds) {
                    jobGroup {
                        'in'("id", command.jobGroupIds)
                    }
                }
                if (command.measuredEventIds) {
                    measuredEvent {
                        'in'("id", command.measuredEventIds)
                    }
                } else if (command.pageIds) {
                    page {
                        'in'("id", command.pageIds)
                    }
                }
                if (command.locationIds) {
                    location {
                        'in'("id", command.locationIds)
                    }
                } else if (command.browserIds) {
                    browser {
                        'in'("id", command.browserIds)
                    }
                }
                isNotNull('customConnectivityName')
            }

            projections {
                distinct('customConnectivityName')
            }
        }
        dtos.addAll(customProfiles.collect {[id:MetaConnectivityProfileId.Custom.value, name: it]})

        def nativeConnectivity = EventResult.createCriteria().list(max: 1) {
            and {
                between("jobResultDate", command.from.toDate(), command.to.toDate())
                if (command.jobGroupIds) {
                    jobGroup {
                        'in'("id", command.jobGroupIds)
                    }
                }
                if (command.measuredEventIds) {
                    measuredEvent {
                        'in'("id", command.measuredEventIds)
                    }
                } else if (command.pageIds) {
                    page {
                        'in'("id", command.pageIds)
                    }
                }
                if (command.locationIds) {
                    location {
                        'in'("id", command.locationIds)
                    }
                } else if (command.browserIds) {
                    browser {
                        'in'("id", command.browserIds)
                    }
                }
                eq('noTrafficShapingAtAll', true)
            }

            projections {
                property('id')
            }
        }
        if (nativeConnectivity) {
            dtos.add([id: MetaConnectivityProfileId.Native.value, name: "Native"])
        }

        println "Took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
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
