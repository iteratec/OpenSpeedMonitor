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

import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import org.joda.time.DateTime
import spock.lang.Specification

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.that

@Build([CsTargetValue, CsTargetGraph])
class CsTargetGraphSpec extends Specification implements BuildDataTest {
    final static double error = 0.01

    void setupSpec() {
        mockDomains(CsTargetValue, CsTargetGraph)
    }

    def "test percent calculation by date"() {
        setup:
        DateTime endDate = new DateTime(2013, 7, 17, 16, 28, 35)
        DateTime endDate120DaysAgo = endDate.minusDays(120)
        CsTargetGraph targetGraph = CsTargetGraph.build(
                pointOne: CsTargetValue.build(date: endDate120DaysAgo.toDate(), csInPercent: 80),
                pointTwo: CsTargetValue.build(date: endDate.toDate(), csInPercent: 90)
        )

        expect: "the target graph can calculate the correct value for a given date in range"
        that targetGraph.getPercentOfDate(endDate120DaysAgo), closeTo(80d, error)
        that targetGraph.getPercentOfDate(endDate.minusDays(90)), closeTo(82.5d, error)
        that targetGraph.getPercentOfDate(endDate.minusDays(60)), closeTo(85d, error)
        that targetGraph.getPercentOfDate(endDate.minusDays(30)), closeTo(87.5d, error)
        that targetGraph.getPercentOfDate(endDate), closeTo(90d, error)
    }
}
