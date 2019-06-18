package de.iteratec.osm.barchart

import de.iteratec.osm.result.DeviceType
import de.iteratec.osm.result.OperatingSystem
import grails.validation.Validateable
import org.joda.time.DateTime

class GetBarchartCommand implements Validateable {
    DateTime from
    DateTime to

    DateTime fromComparative
    DateTime toComparative

    List<Long> pages
    List<Long> jobGroups
    List<String> measurands
    List<Long> browsers
    List<DeviceType> deviceTypes
    List<OperatingSystem> operatingSystems

    String aggregationValue
}
