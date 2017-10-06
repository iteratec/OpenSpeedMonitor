package de.iteratec.osm.result.dao

import de.iteratec.osm.result.SelectedMeasurand
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

