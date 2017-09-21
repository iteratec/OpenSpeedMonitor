package de.iteratec.osm.result.dao

import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.SelectedMeasurandType

class EventResultMedianBuilder {
    EventResultCriteriaBuilder builder = new EventResultCriteriaBuilder()
    private static final String USER_TIMINGS_NAME = 'userTimings'

    EventResultProjection getMedianFor(EventResultCriteriaBuilder baseFilters, SelectedMeasurand selectedMeasurand) {
        if (!selectedMeasurand.selectedType.isUserTiming()) {
            builder.addPropertyProjection(selectedMeasurand.getDatabaseRelevantName(), "value")
            builder.orderBy(selectedMeasurand.getDatabaseRelevantName())
        } else {
            builder.query.createAlias(USER_TIMINGS_NAME, USER_TIMINGS_NAME)
            builder.filterEquals(addAliasForUserTimingField(selectedMeasurand.getDatabaseRelevantName()))
            String relevantField = selectedMeasurand.selectedType == SelectedMeasurandType.USERTIMING_MEASURE ? "duration" : "startTime"
            builder.addPropertyProjection(relevantField, "value")
            builder.orderBy(relevantField)
        }
        builder.mergeWith(baseFilters)
        Double median = calculateMedian(builder.getResults())
        return new EventResultProjection(isAggregation: true, projectedProperties: ["median": median])
    }

    private String addAliasForUserTimingField(String fieldNameInUserTiming) {
        return USER_TIMINGS_NAME + '.' + fieldNameInUserTiming
    }

    private Double calculateMedian(List<Map> aggregations) {
        Double median
        if ((aggregations.size() % 2) != 0) {
            median = aggregations.get((Integer) ((aggregations.size() - 1) / 2)).value;
        } else {
            median = (aggregations.get((Integer) (aggregations.size() / 2)).value +
                    aggregations.get((Integer) (aggregations.size() / 2) + 1).value) / 2
        }
        return median
    }
}
