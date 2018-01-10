package de.iteratec.osm.result.dao

import de.iteratec.osm.dao.ProjectionProperty
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.MeasurandGroup
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.UserTimingType
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
    void configureForSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands){
        this.selectedMeasurands = selectedMeasurands
    }

    @Override
    List<EventResultProjection> getResultsForFilter(List<Closure> baseFilters, List<ProjectionProperty> baseProjections, List<MeasurandTrim> trims) {
        List<Closure> filters = []
        filters.addAll(baseFilters)
        filters.add(buildUserTimingFilter(trims))
        filters.add(buildProjection(baseProjections))
        return createEventResultProjections(executeQuery(filters))
    }

    private Closure buildUserTimingFilter(List<MeasurandTrim> trims){
        List<String> userTimingList = selectedMeasurands.findAll { it.selectedType.isUserTiming() }.collect {
            it.databaseRelevantName
        }
        if (!userTimingList) {
            return null
        }
        trims = trims?:[]
        List<MeasurandTrim> loadTimeTrims = trims.findAll{it.measurandGroup == MeasurandGroup.LOAD_TIMES}
        return{
            userTimings {
                'in' 'name', userTimingList
                loadTimeTrims.each {MeasurandTrim trim ->
                    or{
                        and{
                            "${trim.qualifier.getGormSyntax()}" "startTime", trim.value
                            eq "duration", null
                        }
                        "${trim.qualifier.getGormSyntax()}" "duration", trim.value
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

    EventResultProjection getRelevantProjection(Map dbResult, List<EventResultProjection> projections){
        EventResultProjection relevantProjection = projections.find {EventResultProjection projection ->
            projection.id == dbResult.id
        }
        if(!relevantProjection){
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
