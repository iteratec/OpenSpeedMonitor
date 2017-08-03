package de.iteratec.osm.util

import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.Selected
import de.iteratec.osm.result.SelectedType

/**
 * Created by mwg on 03.08.2017.
 */
class SelectedUtil {
    static String USERTIMING_PREFIX = "_UT_"

    static Selected createSelectedFor(String input, CachedView cachedView){
        if(!input){
            return null
        }
        String name
        SelectedType selectedType
        if(input.startsWith(USERTIMING_PREFIX)){
            name = input.substring(4)
            selectedType = SelectedType.USERTIMING
        }else{
            name = input
            selectedType = SelectedType.MEASURAND
        }
        if(name){
            return new Selected(name: name, selectedType: selectedType, cachedView: cachedView)
        }else{
            return null
        }
    }
}
