package de.iteratec.osm.result

class UserTimingSelectionInfomation {
    String name
    UserTimingType type

   static belongsTo=[ResultSelectionInformation]

    static constraints = {
    }
}
