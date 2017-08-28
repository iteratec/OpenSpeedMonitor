package de.iteratec.osm.result
/**
 * Created by mwg on 02.08.2017.
 */
enum SelectedMeasurandType {
    MEASURAND{
        Double getValue(EventResult eventResult, String name) {
            Measurand measurand = name as Measurand
            return eventResult."$measurand.eventResultField" != null ? Double.valueOf(eventResult."$measurand.eventResultField") : null
        }

        MeasurandGroup getMeasurandGroup(String name) {
            return Measurand.valueOf(name).measurandGroup
        }

        String getOptionPrefix() {
            return ""
        }
    },
    USERTIMING_MARK{
        Double getValue(EventResult eventResult, String name) {
            UserTiming userTiming = eventResult.userTimings.find { it.name == name && it.type == UserTimingType.MARK }
            return userTiming?.getValue()
        }

        MeasurandGroup getMeasurandGroup(String name) {
            return UserTimingType.MARK.getMeasurandGroup()
        }

        String getOptionPrefix() {
            return "_UTMK_"
        }
    },
    USERTIMING_MEASURE{
        Double getValue(EventResult eventResult, String name) {
            UserTiming userTiming = eventResult.userTimings.find {
                it.name == name && it.type == UserTimingType.MEASURE
            }
            return userTiming?.getValue()
        }

        MeasurandGroup getMeasurandGroup(String name) {
            return UserTimingType.MEASURE.getMeasurandGroup()
        }

        String getOptionPrefix() {
            return "_UTME_"
        }
    }

    abstract Double getValue(EventResult eventResult, String name)

    abstract MeasurandGroup getMeasurandGroup(String name)

    abstract String getOptionPrefix()
}