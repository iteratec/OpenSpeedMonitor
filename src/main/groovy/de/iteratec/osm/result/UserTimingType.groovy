package de.iteratec.osm.result

/**
 * Created by mwg on 19.07.2017.
 */
enum UserTimingType {
    MARK(MeasurandGroup.LOAD_TIMES, "userTimes" ),
    MEASURE(MeasurandGroup.LOAD_TIMES, "userTimingMeasures")

    MeasurandGroup measurandGroup
    String tagInResultXml

    private UserTimingType(MeasurandGroup group, String groupTag){
        measurandGroup = group
        tagInResultXml = groupTag
    }
}