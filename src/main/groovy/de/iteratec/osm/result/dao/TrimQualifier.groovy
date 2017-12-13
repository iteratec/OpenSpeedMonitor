package de.iteratec.osm.result.dao

/**
 * @author nkuhn
 */
enum TrimQualifier {

    GREATER_THAN('gt'),
    LOWER_THAN('lt')

    private String gorm

    private TrimQualifier(String gorm){
        this.gorm = gorm
    }
    String getGormSyntax(){
        return this.gorm
    }
}