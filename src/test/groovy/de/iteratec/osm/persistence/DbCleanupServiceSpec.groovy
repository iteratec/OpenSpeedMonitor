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

package de.iteratec.osm.persistence

import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdaterDummy
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.ResultSelectionInformation
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import spock.lang.Specification

@Build([JobResult, EventResult, ResultSelectionInformation, CsiAggregation, CsiAggregationUpdateEvent])
class DbCleanupServiceSpec extends Specification implements BuildDataTest, ServiceUnitTest<DbCleanupService> {

    static final Date OLDER_12_MONTHS = new DateTime(2014,2,9,0,0,0).toDate()
    static final Date NEWER_12_MONTHS = new DateTime().toDate()

    void setup() {
        service.batchActivityService = Stub(BatchActivityService){
            getActiveBatchActivity(_, _, _) >> { Class c, Activity activity, String name, int maxStages, boolean observe ->
                return new BatchActivityUpdaterDummy(name,c.name,activity, maxStages, 5000)
            }
        }
    }

    void setupSpec() {
        mockDomains(JobResult, EventResult, CsiAggregation, CsiAggregationUpdateEvent, BatchActivity,
                ConnectivityProfile, Script)
    }

    void "JobResults before given Date get deleted correctly"() {
        given: "JobResults older and newer 12 months ago exist."
        EventResult.build(jobResult: JobResult.build(date: OLDER_12_MONTHS), jobResultDate: OLDER_12_MONTHS)
        EventResult.build(jobResult: JobResult.build(date: NEWER_12_MONTHS), jobResultDate: NEWER_12_MONTHS)

        when: "deleteResultsDataBefore is called with toDeleteBefore 12 months ago."
        Date twelveMonthsAgo = new DateTime().minusMonths(12).toDate()
        service.deleteResultsDataBefore(twelveMonthsAgo)

        then: "JobResults older 12 months got deleted, the rest still exists."
        JobResult.list().size() == 1
        !JobResult.findByDate(OLDER_12_MONTHS)
        JobResult.findByDate(NEWER_12_MONTHS)
    }

    void "EventResults before given Date get deleted correctly"() {
        given: "JobResults/EventResults older and newer 12 months ago exist."
        EventResult.build(jobResult: JobResult.build(date: OLDER_12_MONTHS), jobResultDate: OLDER_12_MONTHS)
        EventResult.build(jobResult: JobResult.build(date: NEWER_12_MONTHS), jobResultDate: NEWER_12_MONTHS)

        when: "deleteResultsDataBefore is called with toDeleteBefore 12 months ago."
        Date twelveMonthsAgo = new DateTime().minusMonths(12).toDate()
        service.deleteResultsDataBefore(twelveMonthsAgo)

        then: "EventResults older 12 months got deleted, the rest still exists."
        EventResult.list().size() == 1
        !EventResult.findByJobResultDate(OLDER_12_MONTHS)
        EventResult.findByJobResultDate(NEWER_12_MONTHS)
    }

    void "ResultSelectionInformation before given Date get deleted correctly"() {
        given: "JobResults/EventResults older and newer 12 months ago exist."
        ResultSelectionInformation.build(jobResultDate: NEWER_12_MONTHS)
        ResultSelectionInformation.build(jobResultDate: OLDER_12_MONTHS)

        when: "deleteResultsDataBefore is called with toDeleteBefore 12 months ago."
        Date twelveMonthsAgo = new DateTime().minusMonths(12).toDate()
        service.deleteResultsDataBefore(twelveMonthsAgo)

        then: "ResultSelectionInformation older 12 months got deleted, the rest still exists."
        ResultSelectionInformation.list().size() == 1
        !ResultSelectionInformation.findByJobResultDate(OLDER_12_MONTHS)
        ResultSelectionInformation.findByJobResultDate(NEWER_12_MONTHS)
    }

    void "CsiAggregations and corresponding CsiAggregationUpdateEvents before given Date get deleted correctly"() {

        given: "CsiAggregations and CsiAggregationUpdateEvent older and newer 12 months ago exist."
        CsiAggregation csiAggregationOlder12Months = CsiAggregation.build(started: OLDER_12_MONTHS)
        CsiAggregationUpdateEvent.build(csiAggregationId: csiAggregationOlder12Months.id)

        CsiAggregation csiAggregationNewer12Months = CsiAggregation.build(started: NEWER_12_MONTHS)
        CsiAggregationUpdateEvent.build(csiAggregationId: csiAggregationNewer12Months.id)

        when: "deleteCsiAggregationsAndCsiAggregationUpdateEventsBefore is called with toDeleteBefore 12 months ago."
        Date twelveMonthsAgo = new DateTime().minusMonths(12).toDate()
        service.deleteCsiAggregationsAndCsiAggregationUpdateEventsBefore(twelveMonthsAgo)

        then: "CsiAggregations and CsiAggregationUpdateEvent older 12 months got deleted, the rest still exists."
        CsiAggregation.list().size() == 1
        !CsiAggregation.findByStarted(OLDER_12_MONTHS)
        CsiAggregation.findByStarted(NEWER_12_MONTHS)

        CsiAggregationUpdateEvent.list().size() == 1
        !CsiAggregationUpdateEvent.findByCsiAggregationId(csiAggregationOlder12Months.ident())
        CsiAggregationUpdateEvent.findByCsiAggregationId(csiAggregationNewer12Months.ident())

    }
}
