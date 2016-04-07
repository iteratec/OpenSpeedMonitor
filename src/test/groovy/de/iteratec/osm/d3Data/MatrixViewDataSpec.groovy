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

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class MatrixViewDataSpec extends Specification {

    def "initialisation test" () {
        when:
        MatrixViewData matrixViewData = new MatrixViewData()

        then:
        matrixViewData.entries.size() == 0
        matrixViewData.columnNames.size() == 0
        matrixViewData.rowNames.size() == 0

        !matrixViewData.columnLabel.isEmpty()
        !matrixViewData.rowLabel.isEmpty()
        !matrixViewData.weightLabel.isEmpty()
        !matrixViewData.colorBrightLabel.isEmpty()
        !matrixViewData.colorDarkLabel.isEmpty()
    }

    def "addNode adds chart entry to list"() {
        given:
        MatrixViewData matrixViewData = new MatrixViewData()
        MatrixViewEntry entry = new MatrixViewEntry()

        when:
        matrixViewData.addEntry(entry)

        then:
        matrixViewData.entries.size() == 1
        matrixViewData.rowNames.size() == 1
        matrixViewData.columnNames.size() == 1
    }

    def "adding two entries with the same column name will add those entries, but creating only one column of that name"() {
        given:
        MatrixViewData matrixViewData = new MatrixViewData()
        MatrixViewEntry entry = new MatrixViewEntry(columnName: "column", rowName: "row1")
        MatrixViewEntry entry2 = new MatrixViewEntry(columnName: "column", rowName: "row2")

        when:
        matrixViewData.addEntry(entry)
        matrixViewData.addEntry(entry2)

        then:
        matrixViewData.entries.size() == 2
        matrixViewData.rowNames.size() == 2
        matrixViewData.columnNames.size() == 1
    }

    def "adding two entries with the same row name will add those entries, but creating only one row of that name"() {
        given:
        MatrixViewData matrixViewData = new MatrixViewData()
        MatrixViewEntry entry = new MatrixViewEntry(columnName: "column1", rowName: "row")
        MatrixViewEntry entry2 = new MatrixViewEntry(columnName: "column2", rowName: "row")

        when:
        matrixViewData.addEntry(entry)
        matrixViewData.addEntry(entry2)

        then:
        matrixViewData.entries.size() == 2
        matrixViewData.rowNames.size() == 1
        matrixViewData.columnNames.size() == 2
    }

    def "after adding rows an entry with same row does not create new row" () {
        given:
        MatrixViewData matrixViewData = new MatrixViewData()
        Set<String> rows = new HashSet<>()
        rows.add("Row1")
        rows.add("Row2")
        MatrixViewEntry entry = new MatrixViewEntry(columnName: "column1", rowName: "Row1")

        when:
        matrixViewData.addRows(rows)
        matrixViewData.addEntry(entry)

        then:
        matrixViewData.rowNames.size() == 2
    }

    def "after adding columns an entry with same column does not create new column" () {
        given:
        MatrixViewData matrixViewData = new MatrixViewData()
        Set<String> columns = new HashSet<>()
        columns.add("Column1")
        columns.add("Column2")
        MatrixViewEntry entry = new MatrixViewEntry(columnName: "Column1", rowName: "Row1")

        when:
        matrixViewData.addColumns(columns)
        matrixViewData.addEntry(entry)

        then:
        matrixViewData.columnNames.size() == 2
    }
}
