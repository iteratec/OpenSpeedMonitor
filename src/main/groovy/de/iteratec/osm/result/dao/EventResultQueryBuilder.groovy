package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.SelectedMeasurandType
import grails.gorm.DetachedCriteria
import org.grails.datastore.mapping.query.Projections
import org.grails.datastore.mapping.query.Query

/**
 * Created by mwg on 31.08.2017.
 */
class EventResultQueryBuilder {
    private EventResultCriteriaBuilder baseQueryBuilder
    private EventResultMeasurandQueryBuilder measurandQueryBuilder
    private EventResultUserTimingQueryBuilder userTimingQueryBuilder

    EventResultQueryBuilder(Integer minValidLoadtime, Integer maxValidLoadtime) {
        baseQueryBuilder = new EventResultCriteriaBuilder()
        baseQueryBuilder.filterBetween('fullyLoadedTimeInMillisecs', minValidLoadtime, maxValidLoadtime)
    }

    EventResultQueryBuilder withJobResultDateBetween(Date from, Date to) {
        if (from && to) {
            baseQueryBuilder.filterBetween('jobResultDate', from, to)
        }
        return this
    }

    EventResultQueryBuilder withJobGroupIn(List<JobGroup> jobGroups, boolean groupBy = false) {
        baseQueryBuilder.filterIn('jobGroup', jobGroups, groupBy)
        return this
    }

    EventResultQueryBuilder withPageIn(List<Page> pages, boolean groupBy = false) {
        baseQueryBuilder.filterIn('page', pages, groupBy)
        return this
    }

    EventResultQueryBuilder withProjectedBaseProperty(String propertyName) {
        baseQueryBuilder.addPropertyProjection(propertyName)
        return this
    }


    EventResultQueryBuilder withSelectedMeasurandPropertyProjection(SelectedMeasurand selectedMeasurand, String projectionName = null) {
        if (selectedMeasurand.selectedType.isUserTiming()) {
            initUserTimingsQueryBuilder()
            userTimingQueryBuilder.withSelectedMeasurandPropertyProjection(selectedMeasurand, projectionName)
        } else {
            initMeasurandsQueryBuilder()
            measurandQueryBuilder.withSelectedMeasurandPropertyProjection(selectedMeasurand, projectionName)
        }
        return this
    }

    EventResultQueryBuilder withSelectedMeasurandAverageProjection(List<SelectedMeasurand> selectedMeasurands) {
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
            userTimingsResult += userTimingQueryBuilder.getResultsForFilter(baseQueryBuilder)
        }
        if (measurandQueryBuilder) {
            measurandResult += measurandQueryBuilder.getResultsForFilter(baseQueryBuilder)
        }

        return mergeResults(measurandResult, userTimingsResult)
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
