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

package de.iteratec.osm.d3Data

import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

class MatrixViewEntrySpec extends Specification implements GrailsUnitTest {

    def "initialisation test" () {
        when: "a new MatrixViewEntry is created"
        MatrixViewEntry matrixViewEntry = new MatrixViewEntry()

        then: "the MatrixViewEntry has been initialised correctly"
        matrixViewEntry.columnName == MatrixViewEntry.DEFAULT_COLUMN_NAME
        matrixViewEntry.rowName == MatrixViewEntry.DEFAULT_ROW_NAME
        matrixViewEntry.weight == MatrixViewEntry.DEFAULT_CELL_WEIGHT
    }
}
