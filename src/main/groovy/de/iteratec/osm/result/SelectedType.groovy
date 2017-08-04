package de.iteratec.osm.result
/**
 * Created by mwg on 02.08.2017.
 */
enum SelectedType {
    MEASURAND({String name -> name as Measurand},
            {String name -> Measurand.valueOf(name).getMeasurandGroup()},
            null),
    USERTIMING_MARK({ String input -> { UserTiming userTiming -> userTiming.name == input && userTiming.type == UserTimingType.MARK}},
            {String name -> UserTimingType.MARK.getMeasurandGroup()},
            "_UTMK_"),
    USERTIMING_MEASURE({String input -> {UserTiming userTiming -> userTiming.name == input && userTiming.type == UserTimingType.MEASURE}},
            { String input -> UserTimingType.MEASURE.getMeasurandGroup()},
            "_UTME_")

    Closure getEventResultParam
    Closure getMeasurandGroup
    String optionPrefix

    private SelectedType(Closure param, Closure group, String prefix){
        getEventResultParam = param
        getMeasurandGroup = group
        optionPrefix = prefix
    }
}