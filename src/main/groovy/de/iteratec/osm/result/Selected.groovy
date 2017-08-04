package de.iteratec.osm.result

import de.iteratec.osm.util.Constants
import groovy.transform.InheritConstructors

@InheritConstructors
class Selected {
    String name
    CachedView cachedView
    SelectedType selectedType

    Selected(String input, CachedView cachedView){
        this.cachedView = cachedView

        if(isNoUserTiming(input)){
            name = input
            selectedType = SelectedType.MEASURAND
        }else{
            if(input.startsWith(SelectedType.USERTIMING_MARK.optionPrefix)){
                name = input.substring(SelectedType.USERTIMING_MARK.optionPrefix.length())
                selectedType = SelectedType.USERTIMING_MARK
            }else{
                name = input.substring(SelectedType.USERTIMING_MEASURE.optionPrefix.length())
                selectedType = SelectedType.USERTIMING_MEASURE
            }
        }
    }

    @Override
    String toString(){
        return this.name + Constants.UNIQUE_STRING_DELIMITTER + this.cachedView.toString()
    }

    Double getNormalizedValueFrom(EventResult eventResult){
        return eventResult.getNormalizedValueFor(selectedType.getEventResultParam.call(name))
    }

    MeasurandGroup getMeasurandGroup(){
        return selectedType.getMeasurandGroup.call(name)
    }

    static Map createUserTimingOptionFor(String name, UserTimingType type){
        return [name:name,option: type.selectedType.optionPrefix+name]
    }

    static boolean isNoUserTiming(String name){
        return !name.startsWith(SelectedType.USERTIMING_MEASURE.optionPrefix)&& !name.startsWith(SelectedType.USERTIMING_MARK.optionPrefix)
    }
}
