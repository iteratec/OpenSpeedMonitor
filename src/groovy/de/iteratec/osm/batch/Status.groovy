package de.iteratec.osm.batch

/**
 * Representation of the current status from a BatchActivity
 *
 * @author bwo
 */
enum Status {
        ACTIVE, INACTIVE, DONE, CANCELLED

    String getI18nCode(){
        return "de.iteratec.osm.batch.status."+this
    }
}