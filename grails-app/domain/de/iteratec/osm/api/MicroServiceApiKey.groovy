package de.iteratec.osm.api

class MicroServiceApiKey {

    String secretKey
    String microService
    Boolean valid = true

    static mapping = {
        valid(defaultValue: true)
    }

    static constraints = {
        secretKey(nullable: false, blank: false)
        microService(nullable: false, blank: false)
        valid(nullable: false)
    }
}
