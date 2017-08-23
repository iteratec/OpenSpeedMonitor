/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

package de.iteratec.osm.result

import spock.lang.Specification

class ContractSpec extends Specification {

    void "no exception is thrown when argument is not null object"() {
        given: "a not null object"
        Object notNullArgument = new Object()

        when: "the object is passed as an argument"
        Contract.requiresArgumentNotNull("argumentsName", notNullArgument)

        then: "no exception is thrown"
        noExceptionThrown()
    }

    void "null pointer exception is thrown when argument is null object"() {
        given: "a null object"
        Object nullArgument = null

        when: "null is passed as an argument"
        Contract.requiresArgumentNotNull("argumentsName", nullArgument)

        then: "a null pointer exception is thrown"
        thrown NullPointerException
    }
}
