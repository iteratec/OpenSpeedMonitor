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

import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.HttpArchive
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.ServiceMocker
import org.joda.time.DateTime

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(DbCleanupService)
@Mock([JobResult, EventResult, HttpArchive, CsiAggregation, CsiAggregationUpdateEvent, BatchActivity])
class DbCleanupServiceSpec {

    static transactional = false

    DbCleanupService serviceUnderTest
    ServiceMocker mocker

    DateTime executionDateBeforeCleanUpDate = new DateTime(2014,2,9,0,0,0)
    DateTime executionDateAfterCleanUpDate = new DateTime()

    @Before
    void setUp() {
        serviceUnderTest = service
        mocker = ServiceMocker.create()
        mocker.mockBatchActivityService(serviceUnderTest)
        serviceUnderTest.batchActivityService.timer.cancel()//we don't need any updates for this tests
    }

    @Test
    void testDeleteResultsDataBefore() {

        //Create specific data for test
        JobResult jobResultWithBeforeCleanupDate = new JobResult(testId: 'test1', date: executionDateBeforeCleanUpDate.toDate()).save(validate: false)
        JobResult jobResultWithAfterCleanupDate = new JobResult(testId: 'test2', date: executionDateAfterCleanUpDate.toDate()).save(validate: false)

        new EventResult(jobResult: jobResultWithBeforeCleanupDate, jobResultDate: jobResultWithBeforeCleanupDate.date).save(validate: false)
        new HttpArchive(jobResult: jobResultWithBeforeCleanupDate).save(validate: false)

        new EventResult(jobResult: jobResultWithAfterCleanupDate, jobResultDate: jobResultWithAfterCleanupDate.date).save(validate: false)
        new HttpArchive(jobResult: jobResultWithAfterCleanupDate).save(validate: false)

        CsiAggregation csiAggregationWithBeforeCleanupDate = new CsiAggregation(started: executionDateBeforeCleanUpDate.toDate()).save(validate: false)
        new CsiAggregationUpdateEvent(csiAggregationId: csiAggregationWithBeforeCleanupDate.id).save(validate: false)

        CsiAggregation csiAggregationWithAfterCleanupDate = new CsiAggregation(started: executionDateAfterCleanUpDate.toDate()).save(validate: false)
        new CsiAggregationUpdateEvent(csiAggregationId: csiAggregationWithAfterCleanupDate.id).save(validate: false)

        // before DbCleanupJob execution
        assertThat(JobResult.list().size(), is(2))
        assertThat(EventResult.list().size(), is(2))
        assertThat(HttpArchive.list().size(), is(2))

        assertThat(CsiAggregation.list().size(), is(2))
        assertThat(CsiAggregationUpdateEvent.list().size(), is(2))

        //delete all {@link JobResult}s, {@link EventResult}s, {@link HttpArchive}s, {@link CsiAggregation}s, {@link CsiAggregationUpdateEvent}s older then one year (12 months)
        serviceUnderTest.deleteResultsDataBefore(new DateTime().minusMonths(12).toDate())
        serviceUnderTest.deleteCsiAggregationsAndCsiAggregationUpdateEventsBefore(new DateTime().minusMonths(12).toDate())

        //after DbCleanupJob execution
        assertThat(HttpArchive.list().size(), is(1))
        //check that the correct HttpArchive is deleted
        assertThat(HttpArchive.findByJobResult(jobResultWithBeforeCleanupDate), is(nullValue()))
        assertThat(HttpArchive.findByJobResult(jobResultWithAfterCleanupDate), is(notNullValue()))

        assertThat(EventResult.list().size(), is(1))
        //check that the correct EventResult is deleted
        assertThat(EventResult.findByJobResultDate(executionDateBeforeCleanUpDate.toDate()), is(nullValue()))
        assertThat(EventResult.findByJobResultDate(executionDateAfterCleanUpDate.toDate()), is(notNullValue()))

        assertThat(JobResult.list().size(), is(1))
        //check that the correct JobResult is deleted
        assertThat(JobResult.findById(jobResultWithBeforeCleanupDate.ident()), is(nullValue()))
        assertThat(JobResult.findById(jobResultWithAfterCleanupDate.ident()), is(notNullValue()))

        assertThat(CsiAggregationUpdateEvent.list().size(), is(1))
        //check that the correct CsiAggregationUpdateEvent is deleted
        assertThat(CsiAggregationUpdateEvent.findByCsiAggregationId(csiAggregationWithBeforeCleanupDate.ident()), is(nullValue()))
        assertThat(CsiAggregationUpdateEvent.findByCsiAggregationId(csiAggregationWithAfterCleanupDate.ident()), is(notNullValue()))

        assertThat(CsiAggregation.list().size(), is(1))
        //check that the correct CsiAggregation is deleted
        assertThat(CsiAggregation.findByStarted(executionDateBeforeCleanUpDate.toDate()), is(nullValue()))
        assertThat(CsiAggregation.findByStarted(executionDateAfterCleanUpDate.toDate()), is(notNullValue()))

    }
}
