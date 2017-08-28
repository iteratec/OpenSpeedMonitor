package de.iteratec.osm.result

import de.iteratec.osm.util.Constants
import groovy.transform.InheritConstructors

@InheritConstructors
class SelectedMeasurand {
    String name
    CachedView cachedView
    SelectedMeasurandType selectedType

    SelectedMeasurand(String optionValue, CachedView cachedView) {
        this.cachedView = cachedView

        if (!isValid(optionValue)) {
            throw new IllegalArgumentException("Not a valid measurand or user timing: ${optionValue}")
        }

        if (isNoUserTiming(optionValue)) {
            name = optionValue
            selectedType = SelectedMeasurandType.MEASURAND
        } else if (optionValue.startsWith(SelectedMeasurandType.USERTIMING_MARK.optionPrefix)) {
            name = optionValue.substring(SelectedMeasurandType.USERTIMING_MARK.optionPrefix.length())
            selectedType = SelectedMeasurandType.USERTIMING_MARK
        } else if (optionValue.startsWith(SelectedMeasurandType.USERTIMING_MEASURE.optionPrefix)) {
            name = optionValue.substring(SelectedMeasurandType.USERTIMING_MEASURE.optionPrefix.length())
            selectedType = SelectedMeasurandType.USERTIMING_MEASURE
        } else {
            throw new IllegalArgumentException("Not a valid measurand or user timing: ${optionValue}")
        }
    }

    @Override
    String toString() {
        return this.name + Constants.UNIQUE_STRING_DELIMITTER + this.cachedView.toString()
    }

    Double getNormalizedValueFrom(EventResult eventResult) {
        return normalizeValue(selectedType.getValue(eventResult, name))
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

    static Map createUserTimingOptionFor(String name, UserTimingType type) {
        return [name: name, id: type.selectedMeasurandType.optionPrefix + name]
    }

    static Map createDataMapForOptGroupSelect() {
        Map result = [:]
        MeasurandGroup.values().each { measurandGroup ->
            result.put(measurandGroup.toString(), Measurand.values().findAll { it.measurandGroup == measurandGroup })
            if (measurandGroup == MeasurandGroup.LOAD_TIMES) {
                result.put("USER_TIMINGS", [])
            }
        }
        return result
    }

    static boolean isNoUserTiming(String name) {
        return Measurand.values().any { it.toString() == name }
    }

    boolean isValid(String name) {
        name = name ?: ""
        return isNoUserTiming(name) || name.startsWith(SelectedMeasurandType.USERTIMING_MEASURE.optionPrefix) || name.startsWith(SelectedMeasurandType.USERTIMING_MARK.optionPrefix)
    }
}
