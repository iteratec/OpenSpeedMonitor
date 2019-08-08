package de.iteratec.osm.linechart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.EventResultQueryBuilder
import de.iteratec.osm.util.PerformanceLoggingService
import grails.gorm.transactions.Transactional

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG

@Transactional
class LineChartTimeSeriesService {

    PerformanceLoggingService performanceLoggingService

    TimeSeriesChartDTO getTimeSeriesFor(GetLinechartCommand cmd) {
        Date from = cmd.from.toDate()
        Date to = cmd.to.toDate()
        List<JobGroup> jobGroups = null
        if (cmd.jobGroups) {
            jobGroups = JobGroup.findAllByIdInList(cmd.jobGroups)
        }
        List<Page> pages = null
        if (cmd.pages) {
            pages = Page.findAllByIdInList(cmd.pages)
        }
        List<MeasuredEvent> measuredEvents = null
        if (cmd.measuredEvents) {
            measuredEvents = MeasuredEvent.findAllByIdInList(cmd.measuredEvents)
        }
        List<SelectedMeasurand> measurands = cmd.measurands.collect {
            new SelectedMeasurand(it, CachedView.UNCACHED)
        }
        List<EventResultProjection> eventResultProjections
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - with builder', 1) {
            EventResultQueryBuilder queryBuilder
            performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - create query builder', 2) {

                queryBuilder = new EventResultQueryBuilder()
                        .withJobResultDateBetween(from, to)
                        .withJobGroupIdsIn(cmd.jobGroups as List)
                        .withPageIdsIn(cmd.pages as List)
                        .withLocationIdsIn(cmd.locations as List)
                        .withBrowserIdsIn(cmd.browsers as List)
                        .withMeasuredEventIdsIn(cmd.measuredEvents as List)
                        .withSelectedMeasurands(measurands)
            }
            /*performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - append connectivities', 2) {
                appendConnectivity(queryBuildera, queryParams)
            }
            performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - append trims', 2) {
                appendTrims(queryBuildera, queryParams)
            }*/
            performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - actually query the data', 2) {
                eventResultProjections = queryBuilder.getRawData()
            }
        }
        TimeSeriesChartDTO timeSeriesChartDTO = new TimeSeriesChartDTO()
        performanceLoggingService.logExecutionTime(DEBUG, "create DTO for TimeSeriesChart", 1) {
            eventResultProjections.each {EventResultProjection eventResultProjection ->
                Page page
                MeasuredEvent measuredEvent
                Date date = (Date) eventResultProjection.jobResultDate;
                Double value = (Double) eventResultProjection.docCompleteTimeInMillisecs;
                JobGroup jobGroup = (JobGroup) jobGroups.find{jobGroup -> jobGroup.id == eventResultProjection.jobGroupId}
                String identifier = "${jobGroup.name}"
                if (measuredEvents) {
                    measuredEvent = (MeasuredEvent) measuredEvents.find{measuredEventEntry -> measuredEventEntry.id == eventResultProjection.measuredEventId}
                    identifier += " | ${measuredEvent?.name}"
                } else if (pages) {
                    measuredEvent = (MeasuredEvent) MeasuredEvent.findById(eventResultProjection.measuredEventId)
                    identifier += " | ${measuredEvent?.name}"
                }
                if (!timeSeriesChartDTO.series.get(identifier)) {
                    TimeSeries timeSeries = new TimeSeries(jobGroup: jobGroup.name)
                    if (measuredEvent) {
                        timeSeries.setMeasuredEvent(measuredEvent.name)
                    }
                    timeSeriesChartDTO.series.put(identifier, timeSeries)
                }
                def timeSeries = timeSeriesChartDTO.series.get(identifier)
                timeSeries.data.put(date, value)
            }
        }

        return timeSeriesChartDTO
    }
}