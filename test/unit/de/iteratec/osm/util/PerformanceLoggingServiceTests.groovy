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



import grails.test.mixin.*

import org.apache.log4j.Appender
import org.apache.log4j.Layout
import org.apache.log4j.Logger
import org.apache.log4j.SimpleLayout
import org.apache.log4j.WriterAppender
import org.junit.*

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
	 * Tests whether {@link PerformanceLoggingService} logs to rootLogger with log-level {@link LogLevel.ERROR}.
	 */
	@Test
    void testLoggingOfExecutionTime() {
		
		Logger rootLogger = Logger.getRootLogger()
		ByteArrayOutputStream out = new ByteArrayOutputStream()
		Layout layout = new SimpleLayout()
		Appender appender = new WriterAppender(layout, out)
		rootLogger.addAppender(appender)
		
		String descriptionOfClosureToMeasure = "descriptionOfClosureToMeasure"
		serviceUnderTest.logExecutionTime(LogLevel.ERROR, descriptionOfClosureToMeasure, IndentationDepth.NULL){
			Thread.sleep(1100)
		}
		String logMsg = out.toString()
		assertTrue(logMsg.startsWith("ERROR"))
		assertTrue(logMsg.indexOf(descriptionOfClosureToMeasure) > -1)
		String eleapsedInSecondsAsString = logMsg.tokenize().pop()
		assertTrue(eleapsedInSecondsAsString.isDouble())
		assertTrue(Double.valueOf(eleapsedInSecondsAsString)>1)
		
    }
	/**
	 * Tests whether {@link PerformanceLoggingService} doesn't logs to rootLogger with log-level {@link LogLevel.INFO}. 
	 */
	@Test
	void testNoLoggingOfExecutionTimeDueToLogLevel() {
		
		Logger rootLogger = Logger.getRootLogger()
		ByteArrayOutputStream out = new ByteArrayOutputStream()
		Layout layout = new SimpleLayout()
		Appender appender = new WriterAppender(layout, out)
		rootLogger.addAppender(appender)
		
		String descriptionOfClosureToMeasure = "descriptionOfClosureToMeasure"
		serviceUnderTest.logExecutionTime(LogLevel.INFO, descriptionOfClosureToMeasure, , IndentationDepth.NULL){
			Thread.sleep(1)
		}
		assertEquals(0, out.toString().size())
		
	}
}
