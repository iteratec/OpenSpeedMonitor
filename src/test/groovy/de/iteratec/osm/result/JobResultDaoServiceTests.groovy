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

import grails.test.mixin.*

import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(JobResultDaoService)
@Mock([JobResult, EventResult])
class JobResultDaoServiceTests {

	JobResultDaoService serviceUnderTest

	@Before
	void setUp() {
		serviceUnderTest = service
	}

	@Test
	void testFindJobResultByEventResult(){

		JobResult jobResult1 = new JobResult(testId: 'test1').save(validate: false)
		JobResult jobResult2 = new JobResult(testId: 'test2').save(validate: false)

		//find jobResult1

		EventResult eventResult1 = new EventResult(jobResult: jobResult1).save(validate: false)
		jobResult1.save(validate: false)
		JobResult jobResultRetrieved = eventResult1.jobResult
		assertNotNull(jobResultRetrieved)
		assertEquals(jobResult1, jobResultRetrieved)

		EventResult eventResult2 = new EventResult(jobResult: jobResult2).save(validate: false)
		jobResult2.save(validate: false)
		jobResultRetrieved = eventResult2.jobResult
		assertNotNull(jobResultRetrieved)
		assertEquals(jobResult2, jobResultRetrieved)

		//find jobResult2

		EventResult eventResult3 = new EventResult(jobResult: jobResult2).save(validate: false)
		jobResult2.save(validate: false)
		jobResultRetrieved = eventResult3.jobResult
		assertNotNull(jobResultRetrieved)
		assertEquals(jobResult2, jobResultRetrieved)

	}

	/* This case is not possible anymore since jobResult has no eventResult-list anymore
	@Test
	void testFindJobResultByEventResultWithDuplicateEntry(){

		JobResult jobResult1 = new JobResult(testId: 'test1').save(validate: false)

		EventResult eventResult1 = new EventResult().save(validate: false)
		jobResult1.addToEventResults(eventResult1).save(validate: false)
		jobResult1.addToEventResults(eventResult1).save(validate: false)
		JobResult jobResultRetrieved = eventResult1.jobResult
		assertNotNull(jobResultRetrieved)
		assertEquals(jobResult1, jobResultRetrieved)
	}
	//*/

	@Test
	void testTryToFindById() {
		JobResult jobResult1 = new JobResult(testId: 'testTryToFindById-1').save(validate: false);
		JobResult jobResultFound1 = serviceUnderTest.tryToFindById( jobResult1.id );
		assertNotNull(jobResultFound1);
		assertEquals(jobResult1.testId, jobResultFound1.testId)

		JobResult jobResult2 = new JobResult(testId: 'testTryToFindById-2').save(validate: false)
		JobResult jobResultFound2 = serviceUnderTest.tryToFindById( jobResult2.id );
		assertNotNull(jobResultFound2);
		assertEquals(jobResult2.testId, jobResultFound2.testId)

		JobResult jobResultFoundNotExisiting = serviceUnderTest.tryToFindById( jobResult1.id + jobResult2.id );
		assertNull(jobResultFoundNotExisiting);
	}
}
