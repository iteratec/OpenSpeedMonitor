package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.UserTimingType
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

    Closure getMeasurandRawDataProjection = { List<SelectedMeasurand> selectedMeasurands, List<String> additionalProjections ->
        List<SelectedMeasurand> measurands = selectedMeasurands.findAll { !it.selectedType.isUserTiming() }
        if (!measurands) {
            return null
        }
        return {
            projections {
                measurands.each {
                    property it.databaseRelevantName, it.databaseRelevantName
                }
                additionalProjections.each {
                    property it, it
                }
            }
        }
    }

    Closure getUserTimingRawDataProjection = { List<SelectedMeasurand> selectedMeasurands, List<String> additionalProjections ->
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
                    property 'name', 'name'
                    property 'type', 'type'
                    property 'startTime', 'startTime'
                    property 'duration', 'duration'
                }
                additionalProjections.each {
                    property it, it
                }
            }
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
            EventResultProjection eventResultProjection = new EventResultProjection(
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
            relevantProjection = new EventResultProjection(jobGroup: transformedAggregation.jobGroup, page: transformedAggregation.page, isAggregation: true)
            result.add(relevantProjection)
        }
        return relevantProjection
    }

    List<EventResultProjection> getMediansFor(List<JobGroup> jobGroups, List<Page> pages, Date from, Date to, List<SelectedMeasurand> selectedMeasurands) {
        List<EventResultProjection> measurandResult = mediansForMeasurands(jobGroups, pages, from, to, selectedMeasurands)
        List<EventResultProjection> userTimingResult = mediansForUserTimings(jobGroups, pages, from, to, selectedMeasurands)
        return mergeResults(measurandResult, userTimingResult)
    }

    private List<EventResultProjection> getMedians(List<EventResultProjection> eventResultProjections) {
        return eventResultProjections.each {
            it.projectedProperties.each { key, value ->
                it.projectedProperties.put(key, getMedian(value))
            }
        }
    }

    private def getMedian(List data) {
        data.removeAll([null])
        data.sort()
        if (data) {
            if (data.size() == 1) {
                return data.get(0)
            }
            if ((data.size() % 2) != 0) {
                return data.get((Integer) ((data.size() - 1) / 2))
            } else {
                return (data.get((Integer) (data.size() / 2)) +
                        data.get((Integer) ((data.size() - 1) / 2))) / 2
            }
        }
    }

    private List<EventResultProjection> mediansForUserTimings(List<JobGroup> jobGroups, List<Page> pages, Date from, Date to, List<SelectedMeasurand> selectedMeasurands) {
        List<SelectedMeasurand> userTimings = selectedMeasurands.findAll { it.selectedType.isUserTiming() }

        if (!userTimings) {
            return []
        }

        List<Map> transformedAggregations = getFor(jobGroups, pages, from, to, userTimings, getUserTimingRawDataProjection)

        if (!transformedAggregations) {
            return []
        }

        List<EventResultProjection> result = []

        if (pages) {
            Map<String, Map> groupededAggregations = [:].withDefault { [:].withDefault { [] } }
            transformedAggregations.each { ungrouped ->
                def value = ungrouped.type == UserTimingType.MEASURE ? ungrouped.duration : ungrouped.startTime
                groupededAggregations.get(ungrouped.jobGroup.id + '_' + ungrouped.page.id).get(ungrouped.name) << value
            }
            groupededAggregations.each { key, valueMap ->
                Long jobGroupId = key.split("_")[0] as Long
                Long pageId = key.split("_")[1] as Long
                EventResultProjection erp = new EventResultProjection()
                erp.jobGroup = jobGroups.find { it.id == jobGroupId }
                erp.page = pages.find { it.id == pageId }
                valueMap.each { nameKey, valueList ->
                    erp.projectedProperties.put(nameKey, getMedian(valueList))
                }
                result << erp
            }
        } else {
            Map<String, Map> groupededAggregations = [:].withDefault { [:].withDefault { [] } }
            transformedAggregations.each { ungrouped ->
                def value = ungrouped.type == UserTimingType.MEASURE ? ungrouped.duration : ungrouped.startTime
                groupededAggregations.get(ungrouped.jobGroup.id).get(ungrouped.name) << value
            }
            groupededAggregations.each { key, valueMap ->
                Long jobGroupId = key as Long
                EventResultProjection erp = new EventResultProjection()
                erp.jobGroup = jobGroups.find { it.id == jobGroupId }
                valueMap.each { nameKey, valueList ->
                    erp.projectedProperties.put(nameKey, getMedian(valueList))
                }
                result << erp
            }
        }
        return result
    }

    private List<EventResultProjection> mediansForMeasurands(List<JobGroup> jobGroups, List<Page> pages, Date from, Date to, List<SelectedMeasurand> selectedMeasurands) {
        List<SelectedMeasurand> measurandss = selectedMeasurands.findAll { !it.selectedType.isUserTiming() }
        if (!measurandss) {
            return []
        }
        List<Map> transformedAggregations = getFor(jobGroups, pages, from, to, measurandss, getMeasurandRawDataProjection)

        if (!transformedAggregations) {
            return []
        }

        Set<String> measurands = transformedAggregations[0].keySet().findAll { it != "jobGroup" && it != "page" }
        Map<EventResultProjection, Map> groupedAggs = [:]

        if (pages) {
            Map<String, List> groupedList = [:].withDefault { [] }
            transformedAggregations.each { ungrouped ->
                groupedList.get(ungrouped.jobGroup.id + '_' + ungrouped.page.id) << ungrouped
            }

            groupedList.each { k, v ->
                EventResultProjection erp = new EventResultProjection()
                erp.jobGroup = v[0].jobGroup
                erp.page = v[0].page
                groupedAggs.put(erp, v)
            }

            groupedAggs.each { k, v ->
                measurands.each { String measurand ->
                    k.projectedProperties."$measurand" = v.collect {
                        it."$measurand"
                    }
                }
            }
        } else {
            Map<String, List> groupedList = [:].withDefault { [] }
            transformedAggregations.each { ungrouped ->
                groupedList.get(ungrouped.jobGroup.id) << ungrouped
            }

            groupedList.each { k, v ->
                EventResultProjection erp = new EventResultProjection()
                erp.jobGroup = v[0].jobGroup
                groupedAggs.put(erp, v)
            }

            groupedAggs.each { k, v ->
                measurands.each { String measurand ->
                    k.projectedProperties."$measurand" = v.collect {
                        it."$measurand"
                    }
                }
            }
        }

        Set<EventResultProjection> result = groupedAggs.keySet()

        return getMedians(result as List)
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

    List<EventResultProjection> getRawDataFor(List<JobGroup> jobGroups, List<Page> pages, Date from, Date to, SelectedMeasurand selectedMeasurand) {
        Closure projectionMethod
        if (selectedMeasurand.selectedType.isUserTiming()) {
            projectionMethod = getUserTimingRawDataProjection
        } else {
            projectionMethod = getMeasurandRawDataProjection
        }
        return createProjectionForSelectedMeasurand(getFor(jobGroups, pages, from, to, [selectedMeasurand], projectionMethod), selectedMeasurand)
    }

    List<EventResultProjection> createProjectionForSelectedMeasurand(List<Map> transformedData, SelectedMeasurand selectedMeasurand) {
        List<EventResultProjection> result = []
        transformedData.each {
            EventResultProjection erp = new EventResultProjection(jobGroup: it.jobGroup, page: it.page)
            if (selectedMeasurand.selectedType.isUserTiming()) {
                def relevantValue = it.type == UserTimingType.MEASURE ? it.duration : it.startTime
                erp.projectedProperties.put(it.name, relevantValue)
            } else {
                String name = selectedMeasurand.getDatabaseRelevantName()
                erp.projectedProperties.put(name, it."$name")
            }
            result.add(erp)
        }
        return result
    }
}
