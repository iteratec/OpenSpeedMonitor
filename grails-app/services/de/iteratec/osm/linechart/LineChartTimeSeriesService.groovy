package de.iteratec.osm.linechart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
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
        List<MeasuredEvent> measuredEvents = null
        if (cmd.measuredEvents) {
            measuredEvents = MeasuredEvent.findAllByIdInList(cmd.measuredEvents)
        }
        List<Page> pages = null
        if (cmd.pages) {
            pages = Page.findAllByIdInList(cmd.pages)
        }
        List<Location> locations = null
        if (cmd.locations) {
            locations = Location.findAllByIdInList(cmd.locations)
        }
        List<Browser> browsers = null
        if (cmd.browsers) {
            browsers = Browser.findAllByIdInList(cmd.browsers)
        }
        List<ConnectivityProfile> connectivityProfiles = null
        if (cmd.connectivities) {
            connectivityProfiles = ConnectivityProfile.findAllByIdInList(cmd.connectivities)
        }
        List<SelectedMeasurand> measurands = null
        if (cmd.measurands) {
            measurands = cmd.measurands.collect { new SelectedMeasurand(it, CachedView.UNCACHED) }.unique()
        }
        List<PerformanceAspectType> performanceAspectTypes = null
        if (cmd.performanceAspectTypes) {
            performanceAspectTypes = cmd.performanceAspectTypes.collect{it.toString().toUpperCase() as PerformanceAspectType}.unique()
        }

        List<EventResultProjection> eventResultProjections = getResultProjections(cmd, from, to, measurands, performanceAspectTypes)
        return buildDTO(eventResultProjections, jobGroups, measuredEvents, pages, locations, browsers, connectivityProfiles, measurands, performanceAspectTypes)
    }

    private List<EventResultProjection> getResultProjections(GetLinechartCommand cmd, Date from, Date to, List<SelectedMeasurand> measurands, List<PerformanceAspectType> performanceAspectTypes) {
        EventResultQueryBuilder queryBuilder = (EventResultQueryBuilder) performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - with builder', 1) {
            return new EventResultQueryBuilder()
                    .withJobResultDateBetween(from, to)
                    .withJobGroupIdsIn(cmd.jobGroups as List)
                    .withMeasuredEventIdsIn(cmd.measuredEvents as List)
                    .withPageIdsIn(cmd.pages as List)
                    .withLocationIdsIn(cmd.locations as List)
                    .withBrowserIdsIn(cmd.browsers as List)
                    .withConnectivity(cmd.connectivities as List, null, false)
                    .withSelectedMeasurands(measurands)
        }
        if (performanceAspectTypes) {
            queryBuilder = (EventResultQueryBuilder) performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - with builder', 1) {
                queryBuilder.withPerformanceAspects(performanceAspectTypes)
            }
        }

        List<EventResultProjection> eventResultProjections = (List<EventResultProjection>) performanceLoggingService.logExecutionTime(DEBUG, 'getting event-results - actually query the data', 2) {
            queryBuilder.getRawData()
        }
        return eventResultProjections
    }

    private TimeSeriesChartDTO buildDTO(List<EventResultProjection> eventResultProjections, List<JobGroup> jobGroups,
                                        List<MeasuredEvent> measuredEvents, List<Page> pages, List<Location> locations,
                                        List<Browser> browsers, List<ConnectivityProfile> connectivityProfiles,
                                        List<SelectedMeasurand> measurands, List<PerformanceAspectType> performanceAspectTypes) {
        TimeSeriesChartDTO timeSeriesChartDTO = new TimeSeriesChartDTO()
        performanceLoggingService.logExecutionTime(DEBUG, "create DTO for TimeSeriesChart", 1) {
            eventResultProjections.each { EventResultProjection eventResultProjection ->
                Date date = (Date) eventResultProjection.jobResultDate
                String testAgent = eventResultProjection.testAgent
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
                Location location
                if (locations) {
                    location = (Location) locations.find { locationEntry -> locationEntry.id == eventResultProjection.locationId }
                    identifier += " | ${location?.uniqueIdentifierForServer}"
                } else if (browsers) {
                    location = (Location) Location.findById(eventResultProjection.locationId)
                    identifier += " | ${location?.uniqueIdentifierForServer}"
                }
                ConnectivityProfile connectivity
                if (connectivityProfiles) {
                    connectivity = (ConnectivityProfile) connectivityProfiles.find { connectivityProfile -> connectivityProfile.name == eventResultProjection.connectivityProfile }
                    identifier += " | ${connectivity?.name}"
                }

                measurands.each { measurand ->
                    String dataBaseRelevantName = measurand.getDatabaseRelevantName()
                    String measurandName = measurand.getName()
                    Double value = (Double) eventResultProjection."$dataBaseRelevantName"
                    String identifierMeasurand = identifier + " | $measurandName"
                    buildSeries(date, value, testAgent, identifierMeasurand, jobGroup, measuredEvent, location, connectivity, timeSeriesChartDTO)
                    timeSeriesChartDTO.series.find{ it.identifier == identifierMeasurand }.measurand = measurandName
                }

                performanceAspectTypes.each { performanceAspectType ->
                    Double value = (Double) eventResultProjection."$performanceAspectType"
                    String identifierAspect = identifier + " | $performanceAspectType"
                    buildSeries(date, value, testAgent, identifierAspect, jobGroup, measuredEvent, location, connectivity, timeSeriesChartDTO)
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

    private void buildSeries(Date date, Double value, String testAgent, String identifier, JobGroup jobGroup, MeasuredEvent measuredEvent,
                             Location location, ConnectivityProfile connectivity, TimeSeriesChartDTO timeSeriesChartDTO) {
        TimeSeries timeSeries = timeSeriesChartDTO.series.find({ it.identifier == identifier })
        if (!timeSeries) {
            timeSeries = new TimeSeries(
                    identifier: identifier,
                    jobGroup: jobGroup.name
            )
            if (measuredEvent) {
                timeSeries.setMeasuredEvent(measuredEvent.name)
            }
            if (location) {
                timeSeries.setLocation(location.label)
            }
            if (connectivity) {
                timeSeries.setConnectivity(connectivity.name)
            }
            timeSeriesChartDTO.series.add(timeSeries)
        }
        TimeSeriesDataPoint timeSeriesDataPoint = new TimeSeriesDataPoint(date: date, value: value, agent: testAgent)
        timeSeries.data.add(timeSeriesDataPoint)
    }
}
