package de.iteratec.osm.measurement.environment.wptserverproxy

/**
 * Used in {@link ResultPersisterService}.
 * Created by nkuhn on 17.03.16.
 */
class OsmResultPersistanceException extends Exception{
    public OsmResultPersistanceException(String message){
        super(message)
    }
}
