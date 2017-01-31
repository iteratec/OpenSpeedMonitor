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

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.AppenderBase
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import grails.test.mixin.TestFor
import org.slf4j.LoggerFactory
import spock.lang.Specification

/**
 * Test-suite for {@link PerformanceLoggingService}.
 */
@TestFor(PerformanceLoggingService)
class PerformanceLoggingServiceTests extends Specification {

    Logger serviceLogger
    PerformanceLoggingService serviceUnderTest

    void setup() {
        serviceUnderTest = service
        serviceLogger = (Logger) LoggerFactory.getLogger("grails.app.services.de.iteratec.osm.util.PerformanceLoggingService");
    }

    /**
     * Tests whether {@link PerformanceLoggingService} logs to rootLogger with log-level {@link Level.ERROR}.
     */
    void testLoggingOfExecutionTime() {
        given: "We get access to the log output"
        String message
        Appender appender = addAppender { String m -> message = m }
        String descriptionOfClosureToMeasure = "descriptionOfClosureToMeasure"

        when: "We set the log level to Info and execute a operation which logs on Fatal"
        serviceLogger.setLevel(Level.INFO)
        serviceUnderTest.logExecutionTime(LogLevel.FATAL, descriptionOfClosureToMeasure, 0) {
            Thread.sleep(1100)
        }

        then: "We should receive a log entry"
        message.contains("Elapsed Sec")
        message.indexOf(descriptionOfClosureToMeasure) > -1
        String elapsedInSecondsAsString = message.tokenize().pop()
        elapsedInSecondsAsString.isDouble()
        Double.valueOf(elapsedInSecondsAsString) > 1

        cleanup: "Remove the Appender"
        serviceLogger.detachAppender(appender)
    }

    /**
     * Tests whether {@link PerformanceLoggingService} doesn't logs to rootLogger with log-level {@link Level.INFO}.
     */
    void testNoLoggingOfExecutionTimeDueToLogLevel() {
        given: "We get access to the log output"
        String message
        Appender appender = addAppender {
            String m -> message = m
        }
        String descriptionOfClosureToMeasure = "descriptionOfClosureToMeasure"

        when: "We set the log level to Error and execute a operation which logs on Info"
        serviceLogger.setLevel(Level.ERROR)
        serviceUnderTest.logExecutionTime(LogLevel.INFO, descriptionOfClosureToMeasure, 0) {
            Thread.sleep(1)
        }

        then: "We should'nt receive a log entry"
        message == null

        cleanup: "Remove the Appender"
        serviceLogger.detachAppender(appender)
    }

    /**
     * Create and adds an Appender to the serviceLogger.
     *
     * @param c Closure, which ist called when doAppend ist called.{String str -> }* @return
     */
    private Appender addAppender(Closure c) {
        Appender mockAppender = new AppenderBase<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
            }

            @Override
            void doAppend(ILoggingEvent eventObject) {
                c(eventObject.formattedMessage)
                super.doAppend(eventObject)
            }
        }
        mockAppender.setContext(serviceLogger.loggerContext)
        serviceLogger.addAppender(mockAppender);
        return mockAppender
    }
}
