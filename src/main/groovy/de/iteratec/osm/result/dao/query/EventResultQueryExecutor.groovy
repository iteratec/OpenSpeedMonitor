package de.iteratec.osm.result.dao.query

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.result.dao.query.projector.EventResultProjector
import de.iteratec.osm.result.dao.query.projector.ProjectionProperty
import de.iteratec.osm.result.dao.query.transformer.EventResultTransformer
import de.iteratec.osm.result.dao.query.trimmer.EventResultTrimmer
import de.iteratec.osm.result.dao.query.trimmer.MeasurandTrim
import de.iteratec.osm.util.PerformanceLoggingService

class EventResultQueryExecutor {
    private EventResultProjector projector
    private EventResultTransformer transformer
    private EventResultTrimmer trimmer
    List<SelectedMeasurand> selectedMeasurands

    void setUserTimings(List<SelectedMeasurand> selectedMeasurands) {
        this.selectedMeasurands = selectedMeasurands.findAll { it.selectedType.isUserTiming() }
    }

    void setMeasurands(List<SelectedMeasurand> selectedMeasurands) {
        this.selectedMeasurands = selectedMeasurands.findAll { !it.selectedType.isUserTiming() }
    }

    private boolean isNotValid() {
        return this.selectedMeasurands.isEmpty() || !transformer || !projector || !trimmer
    }

    void setProjector(EventResultProjector projector) {
        this.projector = projector
    }

    void setTransformer(EventResultTransformer transformer) {
        this.transformer = transformer
    }

    void setTrimmer(EventResultTrimmer trimmer) {
        this.trimmer = trimmer
    }

    List<EventResultProjection> getResultFor(List<Closure> filters, List<MeasurandTrim> measurandTrims, Set<ProjectionProperty> baseProjections, PerformanceLoggingService performanceLoggingService) {
        if (this.isNotValid()) {
            return []
        }

        List<Closure> queryParts = []
        queryParts.addAll(filters)
        List<Closure> trims = trimmer.buildTrims(selectedMeasurands, measurandTrims)
        queryParts.addAll(trims)
        Closure projection = projector.generateSelectedMeasurandProjectionFor(selectedMeasurands, baseProjections)
        queryParts.add(projection)

        List<Map> rawData = executeQuery(queryParts)
        List<EventResultProjection> result = transformer.transformRawQueryResult(rawData)
        return result
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
