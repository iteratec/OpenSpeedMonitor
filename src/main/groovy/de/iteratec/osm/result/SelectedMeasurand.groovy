package de.iteratec.osm.result

import de.iteratec.osm.result.dao.EventResultProjection
import de.iteratec.osm.util.Constants
import groovy.transform.EqualsAndHashCode
import groovy.transform.InheritConstructors

@EqualsAndHashCode
@InheritConstructors
class SelectedMeasurand {
    String name
    CachedView cachedView
    SelectedMeasurandType selectedType
    String optionValue

    SelectedMeasurand(String optionValue, CachedView cachedView) {
        this.cachedView = cachedView
        this.optionValue = optionValue

        if (!isValid(optionValue)) {
            throw new IllegalArgumentException("Not a valid measurand or user timing: ${optionValue}")
        }

        selectedType = getMeasurandType(optionValue)
        if (selectedType) {
            name = optionValue.substring(selectedType.optionPrefix.length())
        }
    }

    @Override
    String toString() {
        return this.name + Constants.UNIQUE_STRING_DELIMITTER + this.cachedView.toString()
    }

    Double getNormalizedValueFrom(EventResultProjection eventResult) {
        Double value = selectedType.getValue(eventResult, name)
        return normalizeValue(value)
    }

    MeasurandGroup getMeasurandGroup() {
        return selectedType.getMeasurandGroup(name)
    }

    Double normalizeValue(def input) {
        if (!input) {
            return input
        }
        return input / this.getMeasurandGroup().getUnit().getDivisor()
    }

    String getDatabaseRelevantName() {
        return this.selectedType.getDatabaseName(this.name)
    }

    static Map createUserTimingOptionFor(String name, UserTimingType type) {
        return [name: name, id: type.selectedMeasurandType.optionPrefix + name]
    }

    static Map createDataMapForOptGroupSelect() {
        Map result = [:]
        MeasurandGroup.values().each { measurandGroup ->
            result.put(measurandGroup.toString(), Measurand.values().findAll { it.measurandGroup == measurandGroup }.collect {it.toString()})
            if (measurandGroup == MeasurandGroup.LOAD_TIMES) {
                result.put("USER_TIMINGS", [])
                result.put("HERO_TIMINGS", [])
            }
        }
        return result
    }

    static boolean isMeasurand(String name) {
        return Measurand.values().any { it.toString() == name }
    }

    static SelectedMeasurandType getMeasurandType(String name) {
        if (isMeasurand(name)) {
            return SelectedMeasurandType.MEASURAND
        } else if (name.startsWith(SelectedMeasurandType.USERTIMING_MARK.optionPrefix)) {
            return SelectedMeasurandType.USERTIMING_MARK
        } else if (name.startsWith(SelectedMeasurandType.USERTIMING_MEASURE.optionPrefix)) {
            return SelectedMeasurandType.USERTIMING_MEASURE
        } else if (name.startsWith(SelectedMeasurandType.HEROTIMING_MARK.optionPrefix)) {
            return SelectedMeasurandType.HEROTIMING_MARK
        } else {
            throw new IllegalArgumentException("Not a valid measurand or user timing: ${name}")
        }
    }

    static boolean couldBeUserTiming(UserTimingType userTimingType, String name) {
        return name.length() > userTimingType.selectedMeasurandType.optionPrefix.length() && name.startsWith(userTimingType.selectedMeasurandType.optionPrefix)
    }

    boolean isValid(String name) {
        name = name ?: ""
        return isMeasurand(name) || couldBeUserTiming(UserTimingType.MARK, name) || couldBeUserTiming(UserTimingType.MEASURE, name) || couldBeUserTiming(UserTimingType.HERO_MARK, name)
    }
}
