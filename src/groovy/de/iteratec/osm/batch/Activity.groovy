package de.iteratec.osm.batch

/**
 * Describes the goal of a BatchActivity
 */
enum Activity {
        DELETE, UPDATE, CREATE

    String getI18nCode(){
        return "de.iteratec.osm.batch.activity."+this
    }
}