package de.iteratec.osm.result.dao

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.util.PerformanceLoggingService

class MeasurandAverageDataQueryBuilder implements SelectedMeasurandQueryBuilder{
    List<SelectedMeasurand> selectedMeasurands

    @Override
    Closure buildProjection(Set<ProjectionProperty> baseProjections) {
        List<String> measurands = selectedMeasurands.findAll { !it.selectedType.isUserTiming() }.collect{it.databaseRelevantName}
        if (!measurands) {
            return null
        }
        return {
            projections {
                measurands.each {
                    avg it, it
                }
                baseProjections.each {ProjectionProperty pp ->
                    groupProperty pp.dbName, pp.alias
                }
            }
        }
    }

    @Override
    void configureForSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands){
        this.selectedMeasurands = selectedMeasurands
    }

    @Override
    List<EventResultProjection> getResultsForFilter(List<Closure> filters, Set<ProjectionProperty> baseProjections, List<MeasurandTrim> trims, Integer maxValidLoadTime, Integer minValidLoadTime, PerformanceLoggingService performanceLoggingService) {
        filters.add(AggregationUtil.generateMinMaxConstraint(false,selectedMeasurands,maxValidLoadTime,minValidLoadTime))
        return createEventResultProjections(getRawQueryResults(baseFilters, baseProjections, trims), baseProjections)
    }


    protected List<Map> getRawQueryResults(List<Closure> baseFilters, Set<ProjectionProperty> baseProjections, List<MeasurandTrim> trims){
        List<Closure> filters = []
        filters.addAll(baseFilters)
        filters.addAll(buildTrim(trims))
        filters.add(buildProjection(baseProjections))
        return executeQuery(filters)
    }

    private List<Closure> buildTrim(List<MeasurandTrim> trims){
        List<Closure> trimClosures = []
        trims = trims?:[]
        trims.each{MeasurandTrim trim->
            this.selectedMeasurands.each{SelectedMeasurand selectedMeasurand ->
                if (selectedMeasurand.measurandGroup == trim.measurandGroup){
                    trimClosures.add({
                        "${trim.qualifier.getGormSyntax()}" "${selectedMeasurand.databaseRelevantName}", trim.value
                    })
                }
            }
        }
        return trimClosures
    }

    List<EventResultProjection> createEventResultProjections(List<Map> dataFromDb, Set<ProjectionProperty> projectionPropertySet) {
        List<EventResultProjection> eventResultProjections = []
        dataFromDb.each {Map dbResult ->
            EventResultProjection eventResultProjection = new EventResultProjection(
                    id: AggregationUtil.generateGroupKeyForMedianAggregators(dbResult, projectionPropertySet)
            )
            eventResultProjection.projectedProperties = dbResult
            eventResultProjections += eventResultProjection
        }
        return eventResultProjections
    }

    List<Map> executeQuery(List<Closure> queryParts) {
        return EventResult.createCriteria().list {
            queryParts.each {
                applyClosure(it, delegate)
            }
        }
    }

    void applyClosure(Closure closure, def criteriaBuilder) {
        closure.delegate = criteriaBuilder
        closure()
    }
}
