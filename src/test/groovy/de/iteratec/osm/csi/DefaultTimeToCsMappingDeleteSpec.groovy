package de.iteratec.osm.csi

import de.iteratec.osm.csi.transformation.DefaultTimeToCsMappingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Tests the delete functionality of the DefaultTimToCsMappingService
 */
@TestFor(DefaultTimeToCsMappingService)
@Mock([DefaultTimeToCsMapping])
class DefaultTimeToCsMappingDeleteSpec extends Specification{

    void "test simple delete"(){
        when: "We create a DefaultTimeToCsMapping with 1000 entries"
            String name = "default1"
            int amount = 1000
        amount.times {
                new DefaultTimeToCsMapping(name: name,loadTimeInMilliSecs: 0, customerSatisfactionInPercent: 0.5).save()
            }
        then: "We should have 1000 entries"
            DefaultTimeToCsMapping.findByName(name).count() == amount
        when: "Delete all with the name"
            service.deleteDefaultTimeToCsMapping(name)
        then: "There should be no DefaultTimeToCsiMapping left"
            DefaultTimeToCsMapping.findByName(name) == null
    }

    void "test delete with non existing name"(){
        given: "There is a name, which got no DefaultTimeToCsMappings"
            String name = "default2"
        when: "We try to delete all mappings"
            service.deleteDefaultTimeToCsMapping(name)
        then: "There should be an Exception"
            thrown IllegalArgumentException
    }
}
