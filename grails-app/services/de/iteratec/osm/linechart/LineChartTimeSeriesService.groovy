package de.iteratec.osm.linechart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.Measurand
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.PerformanceAspectType
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

        List<PerformanceAspectType> performanceAspectTypes = []
        if (cmd.performanceAspectTypes) {
            performanceAspectTypes = cmd.performanceAspectTypes.collect{it.toString().toUpperCase() as PerformanceAspectType}
        }


        print(performanceAspectTypes)

        List<EventResultProjection> eventResultProjections = getResultProjecions(cmd, from, to, measurands, performanceAspectTypes)
        return buildDTO(eventResultProjections, jobGroups, pages, measuredEvents, measurands, performanceAspectTypes)
    }

    private List<EventResultProjection> getResultProjecions(GetLinechartCommand cmd, Date from, Date to, List<SelectedMeasurand> measurands, List<PerformanceAspectType> performanceAspectTypes) {
        EventResultQueryBuilder queryBuilder = (EventResultQueryBuilder) performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - with builder', 1) {
            return new EventResultQueryBuilder()
                    .withJobResultDateBetween(from, to)
                    .withJobGroupIdsIn(cmd.jobGroups as List)
                    .withPageIdsIn(cmd.pages as List)
                    .withLocationIdsIn(cmd.locations as List)
                    .withBrowserIdsIn(cmd.browsers as List)
                    .withMeasuredEventIdsIn(cmd.measuredEvents as List)
                    .withSelectedMeasurands(measurands)
        }
            if (performanceAspectTypes) {
                performanceAspectTypes.unique()
                queryBuilder = (EventResultQueryBuilder) performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - with builder', 1) {
                    queryBuilder.withPerformanceAspects(performanceAspectTypes)
                }
            }

            /*performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - append connectivities', 2) {
                appendConnectivity(queryBuildera, queryParams)
            }
            performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - append trims', 2) {
                appendTrims(queryBuildera, queryParams)
            }*/

        List<EventResultProjection> eventResultProjections = (List<EventResultProjection>) performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - actually query the data', 2) {
            queryBuilder.getRawData()
        }
        return eventResultProjections
    }

    private TimeSeriesChartDTO buildDTO(List<EventResultProjection> eventResultProjections, List<JobGroup> jobGroups, List<Page> pages, List<MeasuredEvent> measuredEvents, List<SelectedMeasurand> measurands, List<PerformanceAspectType> performanceAspectTypes) {
        TimeSeriesChartDTO timeSeriesChartDTO = new TimeSeriesChartDTO()
        performanceLoggingService.logExecutionTime(DEBUG, "create DTO for TimeSeriesChart", 1) {
            eventResultProjections.each { EventResultProjection eventResultProjection ->
                Date date = (Date) eventResultProjection.jobResultDate
                JobGroup jobGroup = (JobGroup) jobGroups.find { jobGroup -> jobGroup.id == eventResultProjection.jobGroupId }
                String identifier = "${jobGroup.name}"
                MeasuredEvent measuredEvent
                if (measuredEvents) {
                    measuredEvent = (MeasuredEvent) measuredEvents.find { measuredEventEntry -> measuredEventEntry.id == eventResultProjection.measuredEventId }
                    identifier += " | ${measuredEvent?.name}"
                } else if (pages) {
                    measuredEvent = (MeasuredEvent) MeasuredEvent.findById(eventResultProjection.measuredEventId)
                    identifier += " | ${measuredEvent?.name}"
                }

                measurands.unique().each { measurand ->
                    Measurand measurandName = Measurand."${measurand.name}"
                    Double value = (Double) eventResultProjection.projectedProperties."$measurandName.eventResultField"
                    String identifierMeasurand = identifier + " | $measurandName"
                    buildSeries(jobGroup, value, identifierMeasurand, date, measuredEvent, timeSeriesChartDTO)
                    timeSeriesChartDTO.series.find{ it.identifier == identifierMeasurand }.measurand = measurandName.toString()
                }

                performanceAspectTypes.each { performanceAspectType ->
                    Double value = (Double) eventResultProjection.projectedProperties."$performanceAspectType"
                    String identifierAspect = identifier + " | $performanceAspectType"
                    buildSeries(jobGroup, value, identifierAspect, date, measuredEvent, timeSeriesChartDTO)
                    timeSeriesChartDTO.series.find{ it.identifier == identifierAspect }.performanceAspectType = performanceAspectType.toString()
                }
            }
            timeSeriesChartDTO.series.any {
                if (it.data.findAll{it.value == null}.size() == it.data.size()){
                    timeSeriesChartDTO.series.remove(it)
                }
            }
        }
        return timeSeriesChartDTO
    }

    private void buildSeries(JobGroup jobGroup, Double value, String identifier, Date date, MeasuredEvent measuredEvent, TimeSeriesChartDTO timeSeriesChartDTO){
        TimeSeries timeSeries = timeSeriesChartDTO.series.find({ it.identifier == identifier })
        if (!timeSeries) {
            timeSeries = new TimeSeries(
                    identifier: identifier,
                    jobGroup: jobGroup.name
            )
            if (measuredEvent) {
                timeSeries.setMeasuredEvent(measuredEvent.name)
            }
            timeSeriesChartDTO.series.add(timeSeries)
        }
        TimeSeriesDataPoint timeSeriesDataPoint = new TimeSeriesDataPoint(date: date, value: value)
        timeSeries.data.add(timeSeriesDataPoint)
    }
}