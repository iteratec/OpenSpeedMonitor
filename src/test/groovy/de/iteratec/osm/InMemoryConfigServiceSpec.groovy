package de.iteratec.osm

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class InMemoryConfigServiceSpec extends Specification implements ServiceUnitTest<InMemoryConfigService> {

    void "check default values"(){
        when: "In Memory Confing service is not null"
        service != null

        then: "default values are set"
        service.databaseCleanupEnabled == false
        service.areMeasurementsGenerallyEnabled() == false
        service.pauseJobProcessingForOverloadedLocations == false
    }

    void "check switching values for active measurements"(int count, boolean startingValue, boolean expection){
        given: "Status of measurements has been set"
        service.setActiveStatusOfMeasurementsGenerally(startingValue)

        when: "when active status of measurements generally has been switched x amount of times"
        boolean switchTo = !startingValue
        count.times {
            service.setActiveStatusOfMeasurementsGenerally(switchTo)
            switchTo = !switchTo
        }

        then: "active status is as expected"
        service.areMeasurementsGenerallyEnabled() == expection

        where:
        count | startingValue | expection
        1     | true          | false
        2     | true          | true
        3     | true          | false
        4     | true          | true
        1     | false         | true
        2     | false         | false
        3     | false         | true
        4     | false         | false
    }

    void "check database cleanup activation" (){
        when: "database cleanup is activated"
        service.activateDatabaseCleanup()

        then: "value has been set accordingly"
        service.databaseCleanupEnabled == true;
    }

    void "check database cleanup deactivation"(){
        given: "database cleanup is activated"
        service.activateDatabaseCleanup()

        when: "database cleanup is deactivated"
        service.deactivateDatabaseCleanup()

        then: "value has been set accordingly"
        service.databaseCleanupEnabled == false
    }
}
