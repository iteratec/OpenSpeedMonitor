package de.iteratec.osm.result

/**
 * Created by mwg on 19.07.2017.
 */
enum UserTimingType {
    MARK(MeasurandGroup.LOAD_TIMES, "userTimes", SelectedMeasurandType.USERTIMING_MARK ),
    MEASURE(MeasurandGroup.LOAD_TIMES, "userTimingMeasures", SelectedMeasurandType.USERTIMING_MEASURE),
    HERO_MARK(MeasurandGroup.LOAD_TIMES, "heroTimes", SelectedMeasurandType.HEROTIMING_MARK)

    MeasurandGroup measurandGroup
    String tagInResultXml
    SelectedMeasurandType selectedMeasurandType

    private UserTimingType(MeasurandGroup group, String groupTag, SelectedMeasurandType type){
        measurandGroup = group
        tagInResultXml = groupTag
        selectedMeasurandType = type
    }
}