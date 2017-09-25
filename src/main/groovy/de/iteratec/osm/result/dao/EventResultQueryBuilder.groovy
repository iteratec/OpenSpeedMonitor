package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.SelectedMeasurand

/**
 * Created by mwg on 31.08.2017.
 */
class EventResultQueryBuilder {
    private EventResultCriteriaBuilder aggregatedQueryBuilder
    private EventResultCriteriaBuilder rawQueryBuilder
    private EventResultMeasurandQueryBuilder measurandQueryBuilder
    private EventResultUserTimingQueryBuilder userTimingQueryBuilder

    EventResultQueryBuilder(Integer minValidLoadtime, Integer maxValidLoadtime) {
        aggregatedQueryBuilder = new EventResultCriteriaBuilder()
        aggregatedQueryBuilder.filterBetween('fullyLoadedTimeInMillisecs', minValidLoadtime, maxValidLoadtime)
        aggregatedQueryBuilder.filterEquals('cachedView', CachedView.UNCACHED)
        rawQueryBuilder = new EventResultCriteriaBuilder()
        rawQueryBuilder.mergeWith(aggregatedQueryBuilder)
    }

    EventResultQueryBuilder withJobResultDateBetween(Date from, Date to) {
        if (from && to) {
            aggregatedQueryBuilder.filterBetween('jobResultDate', from, to)
            rawQueryBuilder.mergeWith(aggregatedQueryBuilder)
        }
        return this
    }

    EventResultQueryBuilder withJobGroupIn(List<JobGroup> jobGroups) {
        aggregatedQueryBuilder.filterIn('jobGroup', jobGroups, true)
        rawQueryBuilder.filterIn('jobGroup', jobGroups, false)
        return this
    }

    EventResultQueryBuilder withJobGroupEquals(JobGroup jobGroups) {
        aggregatedQueryBuilder.filterEquals('jobGroup', jobGroups)
        rawQueryBuilder.mergeWith(aggregatedQueryBuilder)
        return this
    }

    EventResultQueryBuilder withPageIn(List<Page> pages) {
        aggregatedQueryBuilder.filterIn('page', pages, true)
        rawQueryBuilder.filterIn('page', pages, false)
        return this
    }

    EventResultQueryBuilder withPageEquals(Page pages) {
        aggregatedQueryBuilder.filterEquals('page', pages)
        rawQueryBuilder.mergeWith(aggregatedQueryBuilder)
        return this
    }

    EventResultQueryBuilder withProjectedBaseProperty(String propertyName) {
        rawQueryBuilder.addPropertyProjection(propertyName)
        return this
    }


    EventResultQueryBuilder withSelectedMeasurandsPropertyProjection(List<SelectedMeasurand> selectedMeasurands) {
        List<SelectedMeasurand> measurands = selectedMeasurands.findAll { !it.selectedType.isUserTiming() }
        List<SelectedMeasurand> userTimings = selectedMeasurands.findAll { it.selectedType.isUserTiming() }

        if (measurands) {
            initMeasurandsQueryBuilder()
            measurandQueryBuilder.withMeasurandProjection(measurands)
        }
        if (userTimings) {
            initUserTimingsQueryBuilder()
            userTimingQueryBuilder.withUserTimingsPropertyProjection(userTimings)
        }

        return this
    }

    EventResultQueryBuilder withSelectedMeasurandsAverageProjection(List<SelectedMeasurand> selectedMeasurands) {
        List<SelectedMeasurand> measurands = selectedMeasurands.findAll { !it.selectedType.isUserTiming() }
        List<SelectedMeasurand> userTimings = selectedMeasurands.findAll { it.selectedType.isUserTiming() }

        if (measurands) {
            initMeasurandsQueryBuilder()
            measurandQueryBuilder.withMeasurandsAveragesProjection(measurands)
        }
        if (userTimings) {
            initUserTimingsQueryBuilder()
            userTimingQueryBuilder.withUserTimingsAveragesProjection(userTimings)
        }

        return this
    }

    List<EventResultProjection> getResults() {
        List<EventResultProjection> userTimingsResult = []
        List<EventResultProjection> measurandResult = []

        if (userTimingQueryBuilder) {
            userTimingsResult += userTimingQueryBuilder.getResultsForFilter(aggregatedQueryBuilder)
        }
        if (measurandQueryBuilder) {
            measurandResult += measurandQueryBuilder.getResultsForFilter(aggregatedQueryBuilder)
        }

        return mergeResults(measurandResult, userTimingsResult)
    }

    List<EventResultProjection> getMedians() {
        List<EventResultProjection> userTimingsResult = []
        List<EventResultProjection> measurandResult = []

        if (userTimingQueryBuilder) {
            userTimingsResult += listProjectedValues(userTimingQueryBuilder.getResultsForFilter(rawQueryBuilder))
        }
        if (measurandQueryBuilder) {
            measurandResult += listProjectedValues(measurandQueryBuilder.getResultsForFilter(rawQueryBuilder))
        }

        return mergeResults(measurandResult, userTimingsResult).each {
            it.projectedProperties.each { key, value ->
                it.projectedProperties.put(key, getMedian(value))
            }
        }
    }

    private List<EventResultProjection> mergeResults(List<EventResultProjection> measurandResult, List<EventResultProjection> userTimingResult) {
        if (measurandResult && userTimingResult) {
            measurandResult.each { result ->
                EventResultProjection match = userTimingResult.find { it == result }
                result.projectedProperties.putAll(match.projectedProperties)
            }
        } else {
            return measurandResult ? measurandResult : userTimingResult
        }

    }

    private List<EventResultProjection> listProjectedValues(List<EventResultProjection> unsortedResults) {
        List<EventResultProjection> sortedResults = []
        unsortedResults.each { unsortedResult ->
            EventResultProjection sorted = sortedResults.find { unsortedResult == it }
            if (!sorted) {
                sorted = new EventResultProjection(
                        jobGroup: unsortedResult.jobGroup,
                        page: unsortedResult.page,
                        isAggregation: unsortedResult.isAggregation)
                unsortedResult.projectedProperties.each { key, value ->
                    sorted.projectedProperties.put(key, [])
                }
                sortedResults += sorted
            }
            unsortedResult.projectedProperties.each { key, value ->
                if (value) {
                    sorted.projectedProperties."$key" += value
                }
            }
        }
        return sortedResults
    }

    private def getMedian(List data) {
        data.sort()
        if (data) {
            if (data.size() == 2) {
                return (data.get(0) + data.get(1)) / 2
            }
            if (data.size() == 1) {
                return data.get(0)
            }
            if ((data.size() % 2) != 0) {
                return data.get((Integer) ((data.size() - 1) / 2));
            } else {
                return (data.get((Integer) (data.size() / 2)) +
                        data.get((Integer) (data.size() / 2) + 1)) / 2
            }
        }
    }

    private initUserTimingsQueryBuilder() {
        if (!userTimingQueryBuilder) {
            userTimingQueryBuilder = new EventResultUserTimingQueryBuilder()
        }
    }

    private initMeasurandsQueryBuilder() {
        if (!measurandQueryBuilder) {
            measurandQueryBuilder = new EventResultMeasurandQueryBuilder()
        }
    }
}
