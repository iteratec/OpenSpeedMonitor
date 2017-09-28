package de.iteratec.osm.result

class UserTimingSelectionInformation {
    String name
    UserTimingType type

   static belongsTo=[ResultSelectionInformation]

    static constraints = {
    }
}
