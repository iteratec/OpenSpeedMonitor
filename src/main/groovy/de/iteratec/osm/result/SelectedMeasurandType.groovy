package de.iteratec.osm.result

import de.iteratec.osm.report.chart.RepresentableWptResult

/**
 * Created by mwg on 02.08.2017.
 */
enum SelectedMeasurandType {
    MEASURAND{
        Double getValue(RepresentableWptResult eventResult, String name) {
            Measurand measurand = name as Measurand
            return eventResult."$measurand.eventResultField" != null ? Double.valueOf(eventResult."$measurand.eventResultField") : null
        }

        MeasurandGroup getMeasurandGroup(String name) {
            return Measurand.valueOf(name).measurandGroup
        }

        String getOptionPrefix() {
            return ""
        }

        String getDatabaseName(String name) {
            return Measurand.valueOf(name).eventResultField
        }

        boolean isUserTiming() {
            return false
        }
    },
    USERTIMING_MARK{
        Double getValue(RepresentableWptResult eventResult, String name) {
            //return eventResult."$name" != null ? Double.valueOf(eventResult."$name") : null
            return  getUsertimingValue(eventResult,name,UserTimingType.MARK)
        }

        MeasurandGroup getMeasurandGroup(String name) {
            return UserTimingType.MARK.getMeasurandGroup()
        }

        String getOptionPrefix() {
            return "_UTMK_"
        }

        String getDatabaseName(String name) {
            return name
        }

        boolean isUserTiming() {
            return true
        }
    },
    USERTIMING_MEASURE{
        Double getValue(RepresentableWptResult eventResult, String name) {
            //return eventResult."$name" != null ? Double.valueOf(eventResult."$name") : null
            return  getUsertimingValue(eventResult,name,UserTimingType.MEASURE)
        }

        MeasurandGroup getMeasurandGroup(String name) {
            return UserTimingType.MEASURE.getMeasurandGroup()
        }

        String getOptionPrefix() {
            return "_UTME_"
        }

        String getDatabaseName(String name) {
            return name
        }

        boolean isUserTiming() {
            return true
        }
    }

    abstract boolean isUserTiming()

    abstract Double getValue(RepresentableWptResult eventResult, String name)

    abstract MeasurandGroup getMeasurandGroup(String name)

    abstract String getOptionPrefix()

    abstract String getDatabaseName(String name)

    private static getUsertimingValue(RepresentableWptResult representableWptResult, String name, UserTimingType type){
        if(representableWptResult.hasProperty("userTimings")){
            Set<UserTiming> userTimings = representableWptResult.userTimings
            UserTiming relevantTiming = userTimings.find({it.name == name})
            if(relevantTiming){
                def value = type == UserTimingType.MARK? relevantTiming.startTime : relevantTiming.duration
                return Double.valueOf(value)
            }
        }
        if(representableWptResult."$name" != null){
            return Double.valueOf(representableWptResult."$name")
        }
        return null
    }
}
