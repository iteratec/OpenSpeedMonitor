package de.iteratec.osm.util

import de.iteratec.osm.result.UserTimingSelectionInfomation

/**
 * Created by mwg on 27.07.2017.
 */
class UserTimingUtil {

    static List<UserTimingSelectionInfomation> transformUserTimingsFromEventResultProjections(def userTimingsFromEventResults){
        return deduplicateFromProjections(userTimingsFromEventResults).collect{new UserTimingSelectionInfomation(it)}
    }

    static def deduplicateFromProjections(def userTimingsFromEventResults){
        if(userTimingsFromEventResults.size() > 0){
            return userTimingsFromEventResults.collect{it as List}.flatten().findAll {it != null}.collect {[name: it.name, type: it.type]}.unique(false)
        }else{
            return null
        }
    }
}
