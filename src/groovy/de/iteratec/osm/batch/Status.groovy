package de.iteratec.osm.batch

/**
 * Representation of the current status from a BatchActivity
 *
 * @author bwo
 */
enum Status {
    ACTIVE ('de.iteratec.osm.batch.status.ACTIVE'),
    INACTIVE ('de.iteratec.osm.batch.status.INACTIVE'),
    DONE ('de.iteratec.osm.batch.status.DONE'),
    CANCELLED ('de.iteratec.osm.batch.status.CANCELLED')

    private final String i18nCode

    Status(String i18nCode){
        this.i18nCode = i18nCode
    }

    public String getI18nCode(){
        return this.i18nCode
    }
}