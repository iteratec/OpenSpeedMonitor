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
class BarchartMedianService {

    OsmConfigCacheService osmConfigCacheService
    I18nService i18nService

    List<Map> getTransformedData(List<JobGroup> jobGroups, List<Page> pages, Date from, Date to, List<SelectedMeasurand> selectedMeasurands) {
        return EventResult.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            'eq'('cachedView', CachedView.UNCACHED)
            'between'('fullyLoadedTimeInMillisecs', osmConfigCacheService.getMinValidLoadtime(), osmConfigCacheService.getMaxValidLoadtime())
            if (pages) 'in'('page', pages)
            'in'('jobGroup', jobGroups)
            'between'('jobResultDate', from, to)
            projections {
                selectedMeasurands.each {
                    property it.databaseRelevantName, it.databaseRelevantName
                }
                property 'jobGroup', 'jobGroup'
                if (pages) property 'page', 'page'
            }
        }
    }

    List<Map> getTransformedDataForUserTimings(List<JobGroup> jobGroups, List<Page> pages, Date from, Date to, List<SelectedMeasurand> selectedMeasurands) {
        return EventResult.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            'eq'('cachedView', CachedView.UNCACHED)
            'between'('fullyLoadedTimeInMillisecs', osmConfigCacheService.getMinValidLoadtime(), osmConfigCacheService.getMaxValidLoadtime())
            if (pages) 'in'('page', pages)
            'in'('jobGroup', jobGroups)
            'between'('jobResultDate', from, to)
            userTimings {
                or{
                    selectedMeasurands.each {
                        eq('name', it.getDatabaseRelevantName())
                    }
                }
            }
            projections {
                userTimings {
                    property 'name', 'name'
                    property 'type', 'type'
                    property 'startTime', 'startTime'
                    property 'duration', 'duration'
                }
                property 'jobGroup', 'jobGroup'
                if (pages) property 'page', 'page'
            }
        }
    }

    List<EventResultProjection> getMediansFor(List<JobGroup> jobGroups, List<Page> pages, Date from, Date to, List<SelectedMeasurand> selectedMeasurands) {
        List<EventResultProjection> measurandResult = mediansForMeasurands(jobGroups, pages, from, to, selectedMeasurands)
        List<EventResultProjection> userTimingResult = mediansForUserTimings(jobGroups, pages, from, to, selectedMeasurands)
        return mergeResults(measurandResult, userTimingResult)
    }

    List<EventResultProjection> getMedians(List<EventResultProjection> eventResultProjections) {
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

    List<EventResultProjection> mediansForUserTimings(List<JobGroup> jobGroups, List<Page> pages, Date from, Date to, List<SelectedMeasurand> selectedMeasurands){
        List<SelectedMeasurand> userTimings = selectedMeasurands.findAll {it.selectedType.isUserTiming()}

        if(!userTimings){
            return []
        }

        List<Map> transformedAggregations = getTransformedDataForUserTimings(jobGroups,pages,from,to, userTimings)

        if(!transformedAggregations){
            return []
        }

        List<EventResultProjection> result = []

        if(pages) {
            Map<String, Map> groupededAggregations = [:].withDefault {[:].withDefault {[]}}
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
                Long jobGroupId = key.split("_")[0] as Long
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

    private List<EventResultProjection> mediansForMeasurands(List<JobGroup> jobGroups, List<Page> pages, Date from, Date to, List<SelectedMeasurand> selectedMeasurands){
        List<SelectedMeasurand> measurandss = selectedMeasurands.findAll {!it.selectedType.isUserTiming()}
        if(!measurandss){
           return []
        }
        List<Map> transformedAggregations = getTransformedData(jobGroups,pages,from,to,measurandss)

        if(!transformedAggregations){
            return []
        }

        Set<String> measurands = transformedAggregations[0].keySet().findAll { it != "jobGroup" && it != "page" }
        Map<EventResultProjection, Map> groupedAggs = [:]

        if(pages) {
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
}
