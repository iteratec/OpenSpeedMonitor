package de.iteratec.osm.util

import org.springframework.dao.DataIntegrityViolationException

/**
 * Created by mwg on 11.09.2017.
 */
class DataIntegrityViolationExpectionUtil {

    static String getEntityNameForForeignKeyViolation(DataIntegrityViolationException exception){
        if(exception.cause.SQLState == "23000"){
           return toCamelCase(exception.message.find("(`[A-Za-z_0-9]+`.`[a-z_]+`)").find("(.`[a-z_]+`)").find("[a-z_]+"))
        }
        return null
    }

    private static String toCamelCase( String text ) {
        text = text.replaceAll( "(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
        return text.capitalize()
    }
}
