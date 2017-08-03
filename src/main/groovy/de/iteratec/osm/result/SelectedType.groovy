package de.iteratec.osm.result
/**
 * Created by mwg on 02.08.2017.
 */
enum SelectedType {
    MEASURAND({String name -> name as Measurand}, {String name -> Measurand.valueOf(name).getMeasurandGroup()}),
    USERTIMING({String name -> name}, {String name -> MeasurandGroup.LOAD_TIMES})

    Closure getEventResultParam
    Closure getMeasurandGroup

    private SelectedType(Closure param, Closure group){
        getEventResultParam = param
        getMeasurandGroup = group
    }
}