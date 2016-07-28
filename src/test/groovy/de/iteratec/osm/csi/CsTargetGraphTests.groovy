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

package de.iteratec.osm.csi

import grails.test.mixin.*

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.*
import spock.lang.Specification

/**
 * Test-suite of {@link CsTargetGraph}.
 */
@TestFor(CsTargetGraph)
@Mock([CsTargetValue, CsTargetGraph])
class CsTargetGraphTests extends Specification {

    DateTime now
    DateTime fourMonthsAgo
    static final String graphLabel = 'myGraph'
    static final Double tolerableDeviationDueToRounding = 0.2

    void "setup"() {
        // now = 17.07.2013 - 16:28:35
        now = new DateTime(1374071315000L, DateTimeZone.UTC)
        fourMonthsAgo = now.minusMonths(4)

        Date nowAsDate = now.toDate()
        assert nowAsDate

        Date fourMonthsAgoDate = fourMonthsAgo.toDate()
        assert fourMonthsAgoDate

        CsTargetValue csTargetNow = new CsTargetValue(
                date: nowAsDate,
                csInPercent: 90).save(failOnError: true)

        CsTargetValue csTargetTwoMonthsAgo = new CsTargetValue(
                date: fourMonthsAgoDate,
                csInPercent: 80).save(failOnError: true)

        new CsTargetGraph(
                label: graphLabel,
                defaultVisibility: true,
                pointOne: csTargetTwoMonthsAgo,
                pointTwo: csTargetNow).save(failOnError: true)
    }

    def "testPercentCalculationByDate"() {
        when:
        CsTargetGraph testGraph = CsTargetGraph.findByLabel(graphLabel)

        then:
        Math.abs(testGraph.getPercentOfDate(fourMonthsAgo) - 80d) < tolerableDeviationDueToRounding
        Math.abs(testGraph.getPercentOfDate(now.minusMonths(3)) - 82.5d) < tolerableDeviationDueToRounding
        Math.abs(testGraph.getPercentOfDate(now.minusMonths(2)) - 85d) < tolerableDeviationDueToRounding
        Math.abs(testGraph.getPercentOfDate(now.minusMonths(1)) - 87.5d) < tolerableDeviationDueToRounding
        Math.abs(testGraph.getPercentOfDate(now) - 90d) < tolerableDeviationDueToRounding
    }
}
