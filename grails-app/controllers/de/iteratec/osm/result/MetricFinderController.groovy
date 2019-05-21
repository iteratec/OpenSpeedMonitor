package de.iteratec.osm.result

import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.EventResultQueryBuilder
import de.iteratec.osm.util.ControllerUtils
import grails.validation.Validateable
import org.joda.time.DateTime
import org.springframework.http.HttpStatus

class MetricFinderController {
    ResultSelectionService resultSelectionService


    @RestAction
    def getEventResults(EventResultCommand command) {
        if (command.hasErrors()) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST,
                    "Invalid parameters: " + command.getErrors().fieldErrors.collect { it.field }.join(", "))
            return
        }
        List<SelectedMeasurand> measurands = getAllMeasurands(command)
        List<EventResultProjection> eventResults = new EventResultQueryBuilder()
                .withJobResultDateBetween(command.from.toDate(), command.to.toDate())
                .withPageIdsIn([command.pageId])
                .withJobGroupIdsIn([command.applicationId])
                .withBrowserIdsIn([command.browserId])
                .withSelectedMeasurands(measurands)
                .getRawData(EventResultQueryBuilder.MetaDataSet.TEST_INFO)
        def dtos = eventResults.collect { result -> [
                date: result.projectedProperties.jobResultDate,
                testInfo: [
                        wptUrl: result.projectedProperties.wptServerBaseurl,
                        testId: result.projectedProperties.testId,
                        run: result.projectedProperties.numberOfWptRun,
                        cached: result.projectedProperties.cachedView == CachedView.CACHED,
                        step: result.projectedProperties.oneBasedStepIndexInJourney
                ],
                timings: measurands.collectEntries {
                    [(it.frontendIdentifier): result.projectedProperties[it.databaseRelevantName]]
                }
        ]}
        ControllerUtils.sendObjectAsJSON(response, dtos)
    }

    List<SelectedMeasurand> getAllMeasurands(EventResultCommand command) {
        ResultSelectionCommand resultSelectionCommand = new ResultSelectionCommand(from: command.from, to: command.to, jobGroupIds: [command.applicationId], pageIds: [command.pageId], browserIds: [command.browserId])
        List<SelectedMeasurand> userTimings = resultSelectionService.query(resultSelectionCommand, ResultSelectionService.ResultSelectionType.UserTimings, { existing ->
            projections {
                userTimings {
                    groupProperty('name')
                    groupProperty('type')
                }
            }
        }).collect { SelectedMeasurand.createForUserTiming(it[0], it[1], CachedView.UNCACHED) }.unique(false)
        List<SelectedMeasurand> measurands = Measurand.values()
                .findAll { it.measurandGroup == MeasurandGroup.LOAD_TIMES }
                .collect { new SelectedMeasurand(it.toString(), CachedView.UNCACHED) }
        return measurands + userTimings
    }
}

class EventResultCommand implements Validateable {
    Long applicationId
    Long pageId
    Long browserId
    DateTime from
    DateTime to

    static constraints = {
        applicationId(nullable: false)
        pageId(nullable: false)
        browserId(nullable: false)
        from(nullable: false)
        to(nullable: false)
    }
}
