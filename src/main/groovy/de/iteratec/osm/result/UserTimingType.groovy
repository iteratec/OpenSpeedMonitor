package de.iteratec.osm.result

/**
 * Created by mwg on 19.07.2017.
 */
enum UserTimingType {
    MARK(MeasurandGroup.LOAD_TIMES, "userTimes", SelectedType.USERTIMING_MARK ),
    MEASURE(MeasurandGroup.LOAD_TIMES, "userTimingMeasures", SelectedType.USERTIMING_MEASURE)

    MeasurandGroup measurandGroup
    String tagInResultXml
    SelectedType selectedType

    private UserTimingType(MeasurandGroup group, String groupTag, SelectedType type){
        measurandGroup = group
        tagInResultXml = groupTag
        selectedType = type
    }
}