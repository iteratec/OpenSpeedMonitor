package de.iteratec.osm.result

import de.iteratec.osm.api.dto.JobGroupDto
import de.iteratec.osm.api.dto.MeasuredEventDto
import de.iteratec.osm.api.dto.PageDto
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.util.ControllerUtils
import org.hibernate.FetchMode
import org.joda.time.DateTime
import org.springframework.http.HttpStatus

class ResultSelectionController {
    JobGroupDaoService jobGroupDaoService;

    def getJobGroupsInTimeFrame(ResultSelectionCommand command) {
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
        if (command.measuredEventIds == null && command.pageIds == null) {
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
        def measuredEventDtos = measuredEvents.collect { [
                id: it[0],
                name: it[1],
                testedPage: ([id: it[2], name: it[3]] as PageDto)
        ] as MeasuredEventDto }
        println "Took " + ((DateTime.now().getMillis() - start) / 1000) + " seconds"
        ControllerUtils.sendObjectAsJSON(response, measuredEventDtos)
    }
}

class ResultSelectionCommand {
    DateTime from;
    DateTime to;
    List<Long> jobGroupIds;
    List<Long> pageIds;
    List<Long> measuredEventIds;
}
