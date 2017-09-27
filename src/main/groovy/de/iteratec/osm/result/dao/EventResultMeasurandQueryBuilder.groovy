package de.iteratec.osm.result.dao

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.util.PerformanceLoggingService

/**
 * Created by mwg on 20.09.2017.
 */
class EventResultMeasurandAveragesQueryBuilder implements BaseMeasurandQueryBuilder {
    EventResultMeasurandAveragesQueryBuilder() {
        builder = new EventResultAveragesCriteriaBuilder()
    }

    void configureForSelectedMeasurands(List<SelectedMeasurand> measurands) {
        isAggregated = true
        if (measurands.any { SelectedMeasurand measurand -> measurand.selectedType.isUserTiming() }) {
            throw new IllegalArgumentException("selectedMeasurands must not be user timings")
        }
        measurands.each {
            builder.addAvgProjection(it.getDatabaseRelevantName())
        }
    }
}

class EventResultMeasurandRawDataQueryBuilder implements BaseMeasurandQueryBuilder {
    EventResultMeasurandRawDataQueryBuilder() {
        builder = new EventResultRawDataCriteriaBuilder()
    }

    void configureForSelectedMeasurands(List<SelectedMeasurand> measurands) {
        if (measurands.any { SelectedMeasurand measurand -> measurand.selectedType.isUserTiming() }) {
            throw new IllegalArgumentException("selectedMeasurands must not be user timings")
        }
        measurands.each {
            builder.addPropertyProjection(it.getDatabaseRelevantName())
        }
    }
}

trait BaseMeasurandQueryBuilder extends SelectedMeasurandQueryBuilder {
    List<EventResultProjection> createEventResultProjections(List<Map> normalized) {
        List<EventResultProjection> eventResultProjections = []
        normalized.each {
            EventResultProjection eventResultProjection = new EventResultProjection(
                    jobGroup: it.jobGroup,
                    page: it.page,
                    isAggregation: isAggregated,
            )
            it.remove("jobGroup")
            it.remove("page")
            eventResultProjection.projectedProperties = it
            eventResultProjections += eventResultProjection
        }
        return eventResultProjections
    }
}

class EventResultMeasurandMedianDataQueryBuilder implements SelectedMeasurandQueryBuilder {
    PerformanceLoggingService performanceLoggingService
    EventResultMeasurandMedianDataQueryBuilder(PerformanceLoggingService performanceLoggingService) {
        builder = new EventResultRawDataCriteriaBuilder()
        this.performanceLoggingService = performanceLoggingService
    }

    void configureForSelectedMeasurands(List<SelectedMeasurand> measurands) {
        if (measurands.any { SelectedMeasurand measurand -> measurand.selectedType.isUserTiming() }) {
            throw new IllegalArgumentException("selectedMeasurands must not be user timings")
        }
        measurands.each {
            builder.addPropertyProjection(it.getDatabaseRelevantName())
        }
    }

    @Override
    List<EventResultProjection> getResultsForFilter(EventResultCriteriaBuilder baseFilters) {
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG,"merge filters", 2) {
            this.builder.mergeWith(baseFilters)
        }
        def result = []
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG,"db only", 2) {
            result = builder.getResults()
        }
        List<EventResultProjection> projections
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG,"createEventResultProjections", 2) {
            projections = createEventResultProjections(result)
        }
        return projections
    }


    @Override
    List<EventResultProjection> createEventResultProjections(List<Map> transformedAggregations) {

        Set<String> measurands
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG,"extract measurands", 3) {
            measurands = transformedAggregations[0].keySet().findAll{it != "jobGroup" && it != "page"}
        }
//        Map<EventResultProjection, Map> groupedAggs
//        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG,"group", 3) {
//            groupedAggs = transformedAggregations.groupBy {Map agg ->
//
//                new EventResultProjection(jobGroup: agg.jobGroup, page: agg.page)
//            }
//        }

//        Map<String, List> groupedList = [:].withDefault {[]}
//        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG,"group", 3) {
//            transformedAggregations.each { ungrouped ->
//                groupedList.get(ungrouped.jobGroup.id+'_'+ungrouped.page.id) << ungrouped
//            }
//        }
//        Map<EventResultProjection, Map> groupedAggs = [:]
//        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG,"create erp map", 3) {
//            groupedList.each { k,v ->
//                EventResultProjection erp = new EventResultProjection()
//                erp.jobGroup = v[0].jobGroup
//                erp.page = v[0].page
//                groupedAggs.put(erp,v)
//            }
//        }

        Map<EventResultProjection, Map> groupedAggs = [:].withDefault {[]}
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG,"group", 3) {
            transformedAggregations.each { ungrouped ->
                groupedAggs.get(new EventResultProjection(jobGroup: ungrouped.jobGroup, page: ungrouped.page)) << ungrouped
            }
        }

        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG,"collect", 3) {
            groupedAggs.each{k,v->
                measurands.each {String measurand ->
                    k.projectedProperties."$measurand" = v.collect{
                        it."$measurand"
                    }
                }
            }
        }
        Set<EventResultProjection> result
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG,"get result", 3) {
            result = groupedAggs.keySet()
        }

        return result as List
    }

    private ArrayList<EventResultProjection> groupAggregations(List<Map> transformedAggregations) {
        List<EventResultProjection> mergedResults = []
        transformedAggregations.each { unmergedResult ->
            EventResultProjection merged = mergedResults.find {
                unmergedResult.jobGroup == it.jobGroup && unmergedResult.page == it.page
            }
            if (!merged) {
                merged = new EventResultProjection(
                        jobGroup: unmergedResult.jobGroup,
                        page: unmergedResult.page,
                        isAggregation: false)
                unmergedResult.remove('jobGroup')
                unmergedResult.remove('page')
                unmergedResult.each { key, value ->
                    merged.projectedProperties.put(key, [])
                }
                mergedResults += merged
            }
            unmergedResult.remove('jobGroup')
            unmergedResult.remove('page')
            unmergedResult.each { key, value ->
                if (value) {
                    merged.projectedProperties."$key" += value
                }
            }
        }
        return mergedResults
    }
}
