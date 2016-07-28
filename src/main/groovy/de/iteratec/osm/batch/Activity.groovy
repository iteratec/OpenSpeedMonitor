package de.iteratec.osm.batch

/**
 * Describes the goal of a BatchActivity
 */
enum Activity { 
	DELETE ('de.iteratec.osm.batch.activity.DELETE'), 
	UPDATE ('de.iteratec.osm.batch.activity.UPDATE'), 
	CREATE ('de.iteratec.osm.batch.activity.CREATE')

	private final String i18nCode
	
	Activity (String i18nCode){
		this.i18nCode = i18nCode
	}
    public String getI18nCode(){
        return i18nCode
    }
}