package geb

import groovy.transform.InheritConstructors

/**
 * Ignore GebTests if it is run with test-app -Dgeb.liveTest
 */
@InheritConstructors
class IgnoreGebTestCondition extends Closure<Boolean>{
    Boolean doCall() {
        System.properties.containsKey('geb.liveTest')
    }

}
