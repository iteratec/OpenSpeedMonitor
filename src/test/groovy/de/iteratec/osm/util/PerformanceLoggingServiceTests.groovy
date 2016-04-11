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

package de.iteratec.osm.util

import ch.qos.logback.classic.Logger
import ch.qos.logback.core.Appender
import ch.qos.logback.core.Layout
import ch.qos.logback.core.OutputStreamAppender
import grails.test.mixin.*

import org.junit.*
import ch.qos.logback.classic.Level
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel

/**
 * Test-suite for {@link PerformanceLoggingService}.
 */
@TestFor(PerformanceLoggingService)
class PerformanceLoggingServiceTests {

	PerformanceLoggingService serviceUnderTest

	@Before
	void setUp() {
		serviceUnderTest = service
	}
	@After
	void tearDown() {
	}
	/**
	 * Tests whether {@link PerformanceLoggingService} logs to rootLogger with log-level {@link Level.ERROR}.
	 */
	@Test
    void testLoggingOfExecutionTime() {

		ByteArrayOutputStream out = setLogLevel(Level.ERROR)
		
		String descriptionOfClosureToMeasure = "descriptionOfClosureToMeasure"
		serviceUnderTest.logExecutionTime(LogLevel.FATAL, descriptionOfClosureToMeasure, IndentationDepth.NULL){
			Thread.sleep(1100)
		}
		String logMsg = out.toString()
		assertTrue(logMsg.startsWith("ERROR"))
		assertTrue(logMsg.indexOf(descriptionOfClosureToMeasure) > -1)
		String elapsedInSecondsAsString = logMsg.tokenize().pop()
		assertTrue(elapsedInSecondsAsString.isDouble())
		assertTrue(Double.valueOf(elapsedInSecondsAsString)>1)
		
    }
	/**
	 * Tests whether {@link PerformanceLoggingService} doesn't logs to rootLogger with log-level {@link Level.INFO}.
	 */
	@Test
	void testNoLoggingOfExecutionTimeDueToLogLevel() {

		ByteArrayOutputStream out = setLogLevel(Level.ERROR)

		String descriptionOfClosureToMeasure = "descriptionOfClosureToMeasure"
		serviceUnderTest.logExecutionTime(LogLevel.INFO, descriptionOfClosureToMeasure, IndentationDepth.NULL){
			Thread.sleep(1)
		}
		assertEquals(0, out.toString().size())
		
	}

	private ByteArrayOutputStream setLogLevel(Level level) {
		Logger performanceLoggingServiceLogger = Logger.getLogger("grails.app.services.de.iteratec.osm.util.PerformanceLoggingService")
		performanceLoggingServiceLogger.level = level
		ByteArrayOutputStream out = new ByteArrayOutputStream()
		Appender appender = new OutputStreamAppender(out)
		performanceLoggingServiceLogger.addAppender(appender)
		return out
	}
}
