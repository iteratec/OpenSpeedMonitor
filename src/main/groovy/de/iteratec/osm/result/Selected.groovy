package de.iteratec.osm.result

import de.iteratec.osm.util.Constants
import groovy.transform.InheritConstructors

@InheritConstructors
class Selected {
    String name
    CachedView cachedView
    SelectedType selectedType

    Selected(String optionValue, CachedView cachedView){
        this.cachedView = cachedView

        if(!isValid(optionValue)){
            name = ""
            selectedType = SelectedType.USERTIMING_MEASURE
            return
        }

        if(isNoUserTiming(optionValue)){
            name = optionValue
            selectedType = SelectedType.MEASURAND
        }else{
            if(optionValue.startsWith(SelectedType.USERTIMING_MARK.optionPrefix)){
                name = optionValue.substring(SelectedType.USERTIMING_MARK.optionPrefix.length())
                selectedType = SelectedType.USERTIMING_MARK
            }else{
                name = optionValue.substring(SelectedType.USERTIMING_MEASURE.optionPrefix.length())
                selectedType = SelectedType.USERTIMING_MEASURE
            }
        }
    }

    @Override
    String toString(){
        return this.name + Constants.UNIQUE_STRING_DELIMITTER + this.cachedView.toString()
    }

    Double getNormalizedValueFrom(EventResult eventResult){
        return normalizeValue(eventResult.getValueFor(selectedType.getEventResultParam.call(name)))
    }

    MeasurandGroup getMeasurandGroup(){
        return selectedType.getMeasurandGroup.call(name)
    }

    Double normalizeValue(def input){
        if(!input){
            return input
        }
        return input/this.getMeasurandGroup().getUnit().getDivisor()
    }

    String getDatabaseRelevantName(){
        if(selectedType == SelectedType.MEASURAND){
            return Measurand.valueOf(name).eventResultField
        }else{
            return name
        }
    }

    static Map createUserTimingOptionFor(String name, UserTimingType type){
        return [name:name,option: type.selectedType.optionPrefix+name]
    }

    static boolean isNoUserTiming(String name){
        return Measurand.values().collect{ it.toString() }.contains(name)
    }

    private boolean isValid(String name){
        if(name){
            if(isNoUserTiming(name)){
                return true
            }else{
                return name.length() > SelectedType.USERTIMING_MARK.optionPrefix.length() || name.length() > SelectedType.USERTIMING_MEASURE.optionPrefix.length()
            }
        }
        return false
    }
}
