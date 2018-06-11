package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.result.dao.BarchartEventResultProjection
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.util.I18nService
import grails.transaction.Transactional
import org.hibernate.criterion.CriteriaSpecification

@Transactional
class BarchartQueryAndCalculationService {

    OsmConfigCacheService osmConfigCacheService
    I18nService i18nService

    List<Map> getTransformedData(List<Closure> queryParts) {
        return EventResult.createCriteria().list {
            queryParts.each {
                applyClosure(it, delegate)
            }
        }
    }

    void applyClosure(Closure closure, def criteriabuilder) {
        closure.delegate = criteriabuilder
        closure()
    }

    Closure getBaseClosure() {
        return {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            'eq'('cachedView', CachedView.UNCACHED)
            'between'('fullyLoadedTimeInMillisecs', osmConfigCacheService.getMinValidLoadtime(), osmConfigCacheService.getMaxValidLoadtime())
        }
    }

    Closure getFilters(List<JobGroup> jobGroups, List<Page> pages, Date from, Date to) {
        return {
            if (pages) 'in'('page', pages)
            'in'('jobGroup', jobGroups)
            'between'('jobResultDate', from, to)
        }
    }

    Closure getMeasurandAveragesProjection = { List<SelectedMeasurand> selectedMeasurands, List<String> additionalProjections ->
        List<SelectedMeasurand> measurands = selectedMeasurands.findAll { !it.selectedType.isUserTiming() }
        if (!measurands) {
            return null
        }
        return {
            projections {
                measurands.each {
                    avg it.databaseRelevantName, it.databaseRelevantName
                }
                additionalProjections.each {
                    groupProperty it, it
                }
            }
        }
    }

    Closure getUserTimingAveragesProjection = { List<SelectedMeasurand> selectedMeasurands, List<String> additionalProjections ->
        List<String> userTimingList = selectedMeasurands.findAll { it.selectedType.isUserTiming() }.collect {
            it.databaseRelevantName
        }

        if (!userTimingList) {
            return null
        }

        return {
            userTimings {
                'in' 'name', userTimingList
            }
            projections {
                userTimings {
                    groupProperty 'name', 'name'
                    groupProperty 'type', 'type'
                    avg 'startTime', 'startTime'
                    avg 'duration', 'duration'
                }
                additionalProjections.each {
                    groupProperty it, it
                }
            }
        }
    }

    private List<Map> getFor(List<JobGroup> jobGroups, List<Page> pages, Date from, Date to, List<SelectedMeasurand> selectedMeasurands, Closure specificProjectionGetter) {
        List<Closure> queryParts = []
        List<String> additionalProjections = []
        if (jobGroups) {
            additionalProjections.add('jobGroup')
        }
        if (pages) {
            additionalProjections.add('page')
        }
        queryParts.add(getBaseClosure())
        queryParts.add(getFilters(jobGroups, pages, from, to))
        Closure projection = specificProjectionGetter(selectedMeasurands, additionalProjections)
        if (!projection) {
            return []
        }
        queryParts.add(projection)
        return getTransformedData(queryParts)
    }

    List<EventResultProjection> getAveragesFor(List<JobGroup> jobGroups, List<Page> pages, Date from, Date to, List<SelectedMeasurand> selectedMeasurands) {
        List<EventResultProjection> measurandResult = createEventResultProjectionsForMeasurands(getFor(jobGroups, pages, from, to, selectedMeasurands, getMeasurandAveragesProjection))
        List<EventResultProjection> userTimingResult = createEventResultProjectionsForUserTimings(getFor(jobGroups, pages, from, to, selectedMeasurands, getUserTimingAveragesProjection))
        return mergeResults(measurandResult, userTimingResult)
    }

    private List<EventResultProjection> createEventResultProjectionsForMeasurands(List<Map> transformedAggregations) {
        List<EventResultProjection> eventResultProjections = []
        transformedAggregations.each {
            EventResultProjection eventResultProjection = new BarchartEventResultProjection(
                    jobGroup: it.jobGroup,
                    page: it.page,
                    isAggregation: true,
            )
            it.remove("jobGroup")
            it.remove("page")
            eventResultProjection.projectedProperties = it
            eventResultProjections += eventResultProjection
        }
        return eventResultProjections
    }

    private List<EventResultProjection> createEventResultProjectionsForUserTimings(List<Map> transformedAggregations) {
        List<EventResultProjection> result = []
        transformedAggregations.each { transformedAggregation ->
            def relevantValue = transformedAggregation.type == UserTimingType.MEASURE ? transformedAggregation.duration : transformedAggregation.startTime
            getRelevantProjection(transformedAggregation, result).projectedProperties.put(transformedAggregation.name, relevantValue)
        }
        return result
    }

    private EventResultProjection getRelevantProjection(Map transformedAggregation, List<EventResultProjection> result) {
        EventResultProjection relevantProjection = result.find {
            it.page == transformedAggregation.page && it.jobGroup == transformedAggregation.jobGroup && it."$transformedAggregation.name" == null
        }
        if (!relevantProjection) {
            relevantProjection = new BarchartEventResultProjection(jobGroup: transformedAggregation.jobGroup, page: transformedAggregation.page, isAggregation: true)
            result.add(relevantProjection)
        }
        return relevantProjection
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
}
