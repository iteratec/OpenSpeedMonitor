package de.iteratec.osm.result

import de.iteratec.osm.util.Constants

/**
 * Created by mwg on 02.08.2017.
 */
enum SelectedType {
    MEASURAND,
    USERTIMING
}


class Selected {
    String name
    CachedView cachedView
    SelectedType selectedType

    @Override
    String toString(){
        return this.name + Constants.UNIQUE_STRING_DELIMITTER + this.cachedView.toString()
    }

    Double getNormalizedValueFrom(EventResult eventResult){
        if(selectedType == SelectedType.MEASURAND){
            return eventResult.getNormalizedValueFor(name as Measurand)
        }
        if(selectedType == SelectedType.USERTIMING){
            return eventResult.getNormalizedValueFor(name)
        }
    }

    MeasurandGroup getMeasurandGroup(){
        if(selectedType == SelectedType.MEASURAND){
            return Measurand.valueOf(name).measurandGroup
        }
        if(selectedType == SelectedType.USERTIMING){
            return MeasurandGroup.LOAD_TIMES
        }
    }
}
