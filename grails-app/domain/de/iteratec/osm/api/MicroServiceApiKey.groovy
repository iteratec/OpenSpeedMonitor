package de.iteratec.osm.api

class MicroServiceApiKey {

    String secretKey
    MicroserviceType microService
    Boolean valid = true

    static mapping = {
        valid(defaultValue: true)
    }

    static constraints = {
        secretKey(nullable: false, blank: false)
        microService(nullable: false, inList: MicroserviceType.values() as List)
        valid(nullable: false)
    }
}
