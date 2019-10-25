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
import org.apache.commons.lang.StringUtils

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG

@Transactional
class LineChartTimeSeriesService {

    PerformanceLoggingService performanceLoggingService

    TimeSeriesChartDTO getTimeSeriesFor(GetLinechartCommand cmd) {
        Date from = cmd.from.toDate()
        Date to = cmd.to.toDate()
        List<JobGroup> jobGroups = []
        if (cmd.jobGroups) {
            jobGroups = JobGroup.findAllByIdInList(cmd.jobGroups)
        }
        List<MeasuredEvent> measuredEvents = []
        if (cmd.measuredEvents) {
            measuredEvents = MeasuredEvent.findAllByIdInList(cmd.measuredEvents)
        }
        List<Page> pages = []
        if (cmd.pages) {
            pages = Page.findAllByIdInList(cmd.pages)
        }
        List<Location> locations = []
        if (cmd.locations) {
            locations = Location.findAllByIdInList(cmd.locations)
        }
        List<Browser> browsers = []
        if (cmd.browsers) {
            browsers = Browser.findAllByIdInList(cmd.browsers)
        }
        List<ConnectivityProfile> connectivityProfiles = []
        if (cmd.connectivities) {
            connectivityProfiles = ConnectivityProfile.findAllByIdInList(cmd.connectivities)
        }
        List<SelectedMeasurand> measurands = []
        if (cmd.measurands) {
            measurands = cmd.measurands.collect { new SelectedMeasurand(it, CachedView.UNCACHED) }.unique()
        }
        List<PerformanceAspectType> performanceAspectTypes = []
        if (cmd.performanceAspectTypes) {
            performanceAspectTypes = cmd.performanceAspectTypes.collect {
                it.toString().toUpperCase() as PerformanceAspectType
            }.unique()
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
        if (measurands.size() == 1 && performanceAspectTypes.size() == 0) {
            timeSeriesChartDTO.summaryLabels.put "measurands", "${measurands[0]?.name}"
        }
        if (performanceAspectTypes.size() == 1 && measurands.size() == 0) {
            timeSeriesChartDTO.summaryLabels.put("measurands", "${performanceAspectTypes[0]}")
        }
        JobGroup jobGroup
        if (jobGroups.size() == 1) {
            jobGroup = jobGroups[0]
            timeSeriesChartDTO.summaryLabels.put "application", "${jobGroup?.name}"
        }
        MeasuredEvent measuredEvent
        if (measuredEvents.size() == 1) {
            measuredEvent = measuredEvents[0]
            timeSeriesChartDTO.summaryLabels.put "measuredEvent", "${measuredEvent?.name}"
        }
        Location location
        if (locations.size() == 1) {
            location = locations[0]
            timeSeriesChartDTO.summaryLabels.put "location", "${location?.uniqueIdentifierForServer}"
        }
        ConnectivityProfile connectivity
        if (connectivityProfiles.size() == 1) {
            connectivity = connectivityProfiles[0]
            timeSeriesChartDTO.summaryLabels.put "connectivity", "${connectivity?.name}"
        }

        performanceLoggingService.logExecutionTime(DEBUG, "create DTO for TimeSeriesChart", 1) {
            eventResultProjections.each { EventResultProjection eventResultProjection ->
                String identifier = ""
                Date date = (Date) eventResultProjection.jobResultDate
                if (jobGroups.size() > 1) {
                    jobGroup = (JobGroup) jobGroups.find { jobGroup12 -> jobGroup12.id == eventResultProjection.jobGroupId }
                    identifier = "${jobGroup.name}"
                }
                if (measuredEvents.size() > 1) {
                    measuredEvent = (MeasuredEvent) measuredEvents.find { measuredEventEntry -> measuredEventEntry.id == eventResultProjection.measuredEventId }
                    if (StringUtils.isBlank(identifier)) identifier = "${measuredEvent?.name}"
                    else identifier += " | ${measuredEvent?.name}"
                } else if (measuredEvents.size() == 0) {
                    measuredEvent = (MeasuredEvent) MeasuredEvent.findById(eventResultProjection.measuredEventId)
                    if (StringUtils.isBlank(identifier)) identifier = "${measuredEvent?.name}"
                    else identifier += " | ${measuredEvent?.name}"
                }
                if (locations.size() > 1) {
                    location = (Location) locations.find { locationEntry -> locationEntry.id == eventResultProjection.locationId }
                    if (StringUtils.isBlank(identifier)) identifier = "${location?.uniqueIdentifierForServer}"
                    else identifier += " | ${location?.uniqueIdentifierForServer}"
                } else if (locations.size() == 0) {
                    location = (Location) Location.findById(eventResultProjection.locationId)
                    if (StringUtils.isBlank(identifier)) identifier = "${location?.uniqueIdentifierForServer}"
                    else identifier += " | ${location?.uniqueIdentifierForServer}"
                }
                if (connectivityProfiles.size() > 1) {
                    connectivity = (ConnectivityProfile) connectivityProfiles.find { connectivityProfile -> connectivityProfile.name == eventResultProjection.connectivityProfile }
                    if (StringUtils.isBlank(identifier)) identifier = "${connectivity?.name}"
                    else identifier += " | ${connectivity?.name}"
                } else if (connectivityProfiles.size() == 0) {
                    connectivity = (ConnectivityProfile) ConnectivityProfile.findByName(eventResultProjection.connectivityProfile)
                    if (StringUtils.isBlank(identifier)) identifier = "${connectivity?.name}"
                    else identifier += " | ${connectivity?.name}"
                }

                measurands.each { measurand ->
                    String dataBaseRelevantName = measurand.getDatabaseRelevantName()
                    String measurandName = measurand.getName()
                    Double value = (Double) eventResultProjection."$dataBaseRelevantName"
                    String identifierMeasurand = identifier
                    if ((measurands.size() + performanceAspectTypes.size()) > 1) {
                        if (StringUtils.isBlank(identifier)) identifierMeasurand = "$measurandName"
                        else identifierMeasurand = "$measurandName | " + identifier
                    }
                    buildSeries(value, identifierMeasurand, date, measurandName, jobGroup, measuredEvent, location, connectivity, timeSeriesChartDTO)
                    timeSeriesChartDTO.series.find { it.identifier == identifierMeasurand }.measurand = measurandName
                }

                // TODO status 500
                performanceAspectTypes.each { performanceAspectType ->
                    Double value = (Double) eventResultProjection."$performanceAspectType"
                    String identifierAspect = identifier
                    if ((measurands.size() + performanceAspectTypes.size()) > 1) {
                        if (StringUtils.isBlank(identifier)) identifierAspect = "$performanceAspectType"
                        else identifierAspect = "$performanceAspectType | " + identifier
                    }
                    buildSeries(value, identifierAspect, date, performanceAspectType.toString(), jobGroup, measuredEvent, location, connectivity, timeSeriesChartDTO)
                    timeSeriesChartDTO.series.find {
                        it.identifier == identifierAspect
                    }.performanceAspectType = performanceAspectType.toString()
                }
            }
            timeSeriesChartDTO.series.any {
                if (it.data.findAll { it.value == null }.size() == it.data.size()) {
                    timeSeriesChartDTO.series.remove(it)
                }
            }
        }
        if (timeSeriesChartDTO.series.size() == 1) {
            timeSeriesChartDTO.series.each { TimeSeries timeSeries ->
                timeSeries.identifier = "${timeSeries.measurand} | ${timeSeries.jobGroup} | ${timeSeries.measuredEvent} | ${timeSeries.location} | ${timeSeries.connectivity}"
            }
            timeSeriesChartDTO.summaryLabels.clear()
        } else if (measuredEvents.size() == 0 || locations.size() == 0 || connectivityProfiles.size() == 0) {
            boolean sameMeasuredEvent = connectivityProfiles.size() == 0
            boolean sameLocation = locations.size() == 0
            boolean sameConnectivity = connectivityProfiles.size() == 0

            String measuredEventName = timeSeriesChartDTO.series[0].measuredEvent
            String locationName = timeSeriesChartDTO.series[0].location
            String connectivityName = timeSeriesChartDTO.series[0].connectivity
            timeSeriesChartDTO.series.each { TimeSeries timeSeries ->
                if (!sameMeasuredEvent && !sameLocation && !sameConnectivity) {
                    // return
                }
                sameMeasuredEvent = sameMeasuredEvent && timeSeries.measuredEvent == measuredEventName
                sameLocation = sameLocation && timeSeries.location == locationName
                sameConnectivity = sameConnectivity && timeSeries.connectivity == connectivityName
            }
            if (sameMeasuredEvent) {
                timeSeriesChartDTO.summaryLabels.put "measuredEvent", measuredEventName
            }
            if (sameLocation) {
                timeSeriesChartDTO.summaryLabels.put "location", locationName
            }
            if (sameConnectivity) {
                timeSeriesChartDTO.summaryLabels.put "connectivity", connectivityName
            }
            if (sameMeasuredEvent || sameLocation || sameConnectivity) {
                timeSeriesChartDTO.series.each { TimeSeries timeSeries ->
                    String identifier = ""
                    if (!timeSeriesChartDTO.summaryLabels.get("measurand")) identifier = timeSeries.measurand
                    if (!timeSeriesChartDTO.summaryLabels.get("application")) {
                        if (StringUtils.isBlank(identifier)) identifier = timeSeries.jobGroup
                        else identifier += " | ${timeSeries.jobGroup}"
                    }
                    if (!timeSeriesChartDTO.summaryLabels.get("measuredEvent")) {
                        if (StringUtils.isBlank(identifier)) identifier = timeSeries.measuredEvent
                        else identifier += " | ${timeSeries.measuredEvent}"
                    }
                    if (!timeSeriesChartDTO.summaryLabels.get("location")) {
                        if (StringUtils.isBlank(identifier)) identifier = timeSeries.location
                        else identifier += " | ${timeSeries.location}"
                    }
                    if (!timeSeriesChartDTO.summaryLabels.get("connectivity")) {
                        if (StringUtils.isBlank(identifier)) identifier = timeSeries.connectivity
                        else identifier += " | ${timeSeries.connectivity}"
                    }
                    timeSeries.identifier = identifier
                }
            }
            timeSeriesChartDTO.series.sort { it.identifier.toUpperCase() }
        }
        return timeSeriesChartDTO
    }

    private void buildSeries(Double value, String identifier, Date date, String measurandName, JobGroup jobGroup,
                             MeasuredEvent measuredEvent, Location location, ConnectivityProfile connectivity,
                             TimeSeriesChartDTO timeSeriesChartDTO) {
        TimeSeries timeSeries = timeSeriesChartDTO.series.find({ it.identifier == identifier })
        if (!timeSeries) {
            timeSeries = new TimeSeries(
                    identifier: identifier,
                    measurand: measurandName,
                    jobGroup: jobGroup.name,
                    measuredEvent: measuredEvent.name,
                    location: location.uniqueIdentifierForServer,
                    connectivity: connectivity.name
            )
            timeSeriesChartDTO.series.add(timeSeries)
        }
        TimeSeriesDataPoint timeSeriesDataPoint = new TimeSeriesDataPoint(date: date, value: value)
        timeSeries.data.add(timeSeriesDataPoint)
    }
}
