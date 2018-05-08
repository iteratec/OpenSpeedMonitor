package de.iteratec.osm.result.dao

import de.iteratec.osm.dao.ProjectionProperty
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasurandGroup
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.util.PerformanceLoggingService

/**
 * Created by mwg on 11.10.2017.
 */
class UserTimingRawDataQueryBuilder implements SelectedMeasurandQueryBuilder {

    List<SelectedMeasurand> selectedMeasurands

    @Override
    Closure buildProjection(List<ProjectionProperty> baseProjections) {

        return {
            projections {
                userTimings {
                    property 'name', 'name'
                    property 'type', 'type'
                    property 'startTime', 'startTime'
                    property 'duration', 'duration'
                }
                baseProjections.each { ProjectionProperty pp ->
                    property pp.dbName, pp.alias
                }
            }
        }
    }

    @Override
    void configureForSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands) {
        this.selectedMeasurands = selectedMeasurands
    }

    @Override
    List<EventResultProjection> getResultsForFilter(List<Closure> baseFilters, List<ProjectionProperty> baseProjections, List<MeasurandTrim> trims, PerformanceLoggingService performanceLoggingService) {
        List<Closure> filters = []
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - add base filters', 4) {
            filters.addAll(baseFilters)
        }
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - build and add ut filters', 4) {
            filters.add(buildUserTimingFilter(trims))
        }
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - build and add projections', 4) {
            filters.add(buildProjection(baseProjections))
        }
        List<Map> dbResult
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - execute ut query', 4) {
            dbResult = executeQuery(filters)
        }
        List<EventResultProjection> projections
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - create EventResultProjections from db maps', 4) {
            projections = createEventResultProjections(dbResult)
        }
        return projections
    }

    private Closure buildUserTimingFilter(List<MeasurandTrim> trims) {
        List<String> userTimingList = selectedMeasurands.findAll { it.selectedType.isUserTiming() }.collect {
            it.databaseRelevantName
        }
        if (!userTimingList) {
            return null
        }
        trims = trims ?: []
        List<MeasurandTrim> loadTimeTrims = trims.findAll { it.measurandGroup == MeasurandGroup.LOAD_TIMES }
        return {
            userTimings {
                'in' 'name', userTimingList
                loadTimeTrims.each { MeasurandTrim trim ->
                    or {
                        and {
                            "${trim.qualifier.getGormSyntax()}" "startTime", new Double(trim.value)
                            isNull("duration")
                        }
                        "${trim.qualifier.getGormSyntax()}" "duration", new Double(trim.value)
                    }
                }
            }
        }
    }

    List<EventResultProjection> createEventResultProjections(List<Map> dataFromDb) {
        List<EventResultProjection> projections = []
        dataFromDb.each { Map dbResult ->
            def userTimingValue = dbResult.type == UserTimingType.MEASURE ? dbResult.duration : dbResult.startTime
            EventResultProjection projection = getRelevantProjection(dbResult, projections)
            dbResult.remove('id')
            projection.projectedProperties.putAll(dbResult)
            projection.projectedProperties.put(dbResult.name, userTimingValue)
        }
        return projections
    }

    EventResultProjection getRelevantProjection(Map dbResult, List<EventResultProjection> projections) {
        EventResultProjection relevantProjection = projections.find { EventResultProjection projection ->
            projection.id == dbResult.id
        }
        if (!relevantProjection) {
            relevantProjection = new EventResultProjection(id: dbResult.id)
            projections.add(relevantProjection)
        }
        return relevantProjection
    }

    List<Map> executeQuery(List<Closure> queryParts) {
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
}
