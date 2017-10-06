package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.util.PerformanceLoggingService

/**
 * Created by mwg on 31.08.2017.
 */
class EventResultQueryBuilder {
    private List<EventResultCriteriaBuilder> filters
    private EventResultAveragesCriteriaBuilder aggregatedQueryBuilder
    private EventResultRawDataCriteriaBuilder rawQueryBuilder
    private SelectedMeasurandQueryBuilder measurandRawQueryBuilder, measurandAveragesQueryBuilder, userTimingRawQueryBuilder, userTimingAveragesQueryBuilder
    PerformanceLoggingService performanceLoggingService


    EventResultQueryBuilder(Integer minValidLoadtime, Integer maxValidLoadtime, PerformanceLoggingService performanceLoggingService
    ) {
        this.performanceLoggingService = performanceLoggingService
        aggregatedQueryBuilder = new EventResultAveragesCriteriaBuilder()
        rawQueryBuilder = new EventResultRawDataCriteriaBuilder()
        filters = [aggregatedQueryBuilder, rawQueryBuilder]

        filters*.filterBetween('fullyLoadedTimeInMillisecs', minValidLoadtime, maxValidLoadtime)
        filters*.filterEquals('cachedView', CachedView.UNCACHED)
    }

    EventResultQueryBuilder withJobResultDateBetween(Date from, Date to) {
        if (from && to) {
            filters*.filterBetween('jobResultDate', from, to)
        }
        return this
    }

    EventResultQueryBuilder withJobGroupIn(List<JobGroup> jobGroups, boolean project = true) {
        filters*.filterIn('jobGroup', jobGroups, project)
        return this
    }

    EventResultQueryBuilder withJobGroupEquals(JobGroup jobGroup) {
        filters*.filterEquals('jobGroup', jobGroup)
        return this
    }

    EventResultQueryBuilder withPageIn(List<Page> pages, boolean project = true) {
        filters*.filterIn('page', pages, project)
        return this
    }

    EventResultQueryBuilder withPageEquals(Page page) {
        filters*.filterEquals('page', page)
        return this
    }

    EventResultQueryBuilder withProjectedBaseProperty(String propertyName) {
        rawQueryBuilder.addPropertyProjection(propertyName)
        return this
    }


    EventResultQueryBuilder withSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands) {
        List<SelectedMeasurand> measurands = selectedMeasurands.findAll { !it.selectedType.isUserTiming() }
        List<SelectedMeasurand> userTimings = selectedMeasurands.findAll { it.selectedType.isUserTiming() }

        if (measurands) {
            initMeasurandsQueryBuilder()
            measurandRawQueryBuilder.configureForSelectedMeasurands(measurands)
            measurandAveragesQueryBuilder.configureForSelectedMeasurands(measurands)
        }
        if (userTimings) {
            initUserTimingsQueryBuilder()
            userTimingRawQueryBuilder.configureForSelectedMeasurands(userTimings)
            userTimingAveragesQueryBuilder.configureForSelectedMeasurands(userTimings)
        }

        return this
    }

    List<EventResultProjection> getRawData() {
        return getResultFor(rawQueryBuilder, userTimingRawQueryBuilder, measurandRawQueryBuilder)
    }

    List<EventResultProjection> getAverages() {
        return getResultFor(aggregatedQueryBuilder, userTimingAveragesQueryBuilder, measurandAveragesQueryBuilder)
    }

    private getResultFor(EventResultCriteriaBuilder filters, SelectedMeasurandQueryBuilder userTimingsBuilder, SelectedMeasurandQueryBuilder measurandsBuilder) {
        List<EventResultProjection> userTimingsResult = []
        List<EventResultProjection> measurandResult = []

        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, "get calculated data from database", 1, {
            if (userTimingsBuilder) {
                userTimingsResult += userTimingsBuilder.getResultsForFilter(filters)
            }
            if (measurandsBuilder) {
                measurandResult += measurandsBuilder.getResultsForFilter(filters)
            }
        })

        return mergeResults(measurandResult, userTimingsResult)
    }

    private List<EventResultProjection> mergeResults(List<EventResultProjection> measurandResult, List<EventResultProjection> userTimingResult) {
        if (measurandResult && userTimingResult) {
            measurandResult.each { result ->
                EventResultProjection match = userTimingResult.find { it == result }
                if (match) {
                    result.projectedProperties.putAll(match.projectedProperties)
                }
            }
        } else {
            return measurandResult ? measurandResult : userTimingResult
        }

    }

    private initUserTimingsQueryBuilder() {
        if (!userTimingRawQueryBuilder) {
            userTimingRawQueryBuilder = new EventResultUserTimingRawDataQueryBuilder()
        }
        if (!userTimingAveragesQueryBuilder) {
            userTimingAveragesQueryBuilder = new EventResultUserTimingAveragesQueryBuilder()
        }
    }

    private initMeasurandsQueryBuilder() {
        if (!measurandRawQueryBuilder) {
            measurandRawQueryBuilder = new EventResultMeasurandRawDataQueryBuilder()
        }
        if (!measurandAveragesQueryBuilder) {
            measurandAveragesQueryBuilder = new EventResultMeasurandAveragesQueryBuilder()
        }
    }
}
