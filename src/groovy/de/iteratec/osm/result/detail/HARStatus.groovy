package de.iteratec.osm.result.detail

/**
 * Created by benjamin on 24.08.15.
 */
public enum HARStatus {
    //If we never tried to cache this archive
    NOT_PERSISTED(0),
    //If we already got this HAR
    PERSISTED(1),
    //If we couldn't connect to the host or we didn't got a matching event result
    NOT_AVAILABLE(2),
    private final Integer value

    HARStatus(Integer value) {
        this.value = value
    }

    Integer getId(){
        value
    }
}