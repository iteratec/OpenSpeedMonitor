package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.util.PerformanceLoggingService
import org.hibernate.criterion.CriteriaSpecification

/**
 * Created by mwg on 31.08.2017.
 */
class EventResultQueryBuilder {
    private List<Closure> filters
    private List<String> additionalProjections
    private SelectedMeasurandQueryBuilder measurandRawQueryBuilder, userTimingRawQueryBuilder

    EventResultQueryBuilder(Integer minValidLoadtime, Integer maxValidLoadtime) {
        filters = []
        additionalProjections = []
        filters.add(getBaseClosure(minValidLoadtime, maxValidLoadtime))
    }

    Closure getBaseClosure(Integer minValidLoadtime, Integer maxValidLoadtime) {
        return {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            'eq'('cachedView', CachedView.UNCACHED)
            'between'('fullyLoadedTimeInMillisecs', minValidLoadtime, maxValidLoadtime)
        }
    }

    EventResultQueryBuilder withJobResultDateBetween(Date from, Date to) {
        if (from && to) {
            filters.add({
                'between' 'jobResultDate', from, to
            })
        }
        return this
    }

    EventResultQueryBuilder withJobGroupIn(List<JobGroup> jobGroups, boolean project = true) {
        if (jobGroups) {
            filters.add({
                'in' 'jobGroup', jobGroups
            })
        }
        if (project) {
            additionalProjections.add('jobGroup')
        }
        return this
    }

    EventResultQueryBuilder withPageIn(List<Page> pages, boolean project = true) {
        if (pages) {
            filters.add({
                'in' 'page', pages
            })
        }
        if (project) {
            additionalProjections.add('page')
        }
        return this
    }


    EventResultQueryBuilder withSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands) {
        List<SelectedMeasurand> measurands = selectedMeasurands.findAll { !it.selectedType.isUserTiming() }
        List<SelectedMeasurand> userTimings = selectedMeasurands.findAll { it.selectedType.isUserTiming() }

        if (measurands) {
            initMeasurandsQueryBuilder()
            measurandRawQueryBuilder.configureForSelectedMeasurands(measurands)
        }
        if (userTimings) {
            initUserTimingsQueryBuilder()
            userTimingRawQueryBuilder.configureForSelectedMeasurands(userTimings)
        }

        return this
    }

    List<EventResultProjection> getRawData() {
        return getResultFor(userTimingRawQueryBuilder, measurandRawQueryBuilder)
    }

    private getResultFor(SelectedMeasurandQueryBuilder userTimingsBuilder, SelectedMeasurandQueryBuilder measurandsBuilder) {
        List<EventResultProjection> userTimingsResult = []
        List<EventResultProjection> measurandResult = []

        if (userTimingsBuilder) {
            userTimingsResult += userTimingsBuilder.getResultsForFilter(filters, additionalProjections)
        }
        if (measurandsBuilder) {
            measurandResult += measurandsBuilder.getResultsForFilter(filters, additionalProjections)
        }

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
            userTimingRawQueryBuilder = new UserTimingRawDataQueryBuilder()
        }
    }

    private initMeasurandsQueryBuilder() {
        if (!measurandRawQueryBuilder) {
            measurandRawQueryBuilder = new MeasurandRawDataQueryBuilder()
        }
    }
}
