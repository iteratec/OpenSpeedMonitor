package de.iteratec.osm.system

class MissingJobResultCheck {

    Date date
    Integer missingResults

    static constraints = {
        date(nullable: false)
        missingResults(nullable: false)
    }
}
