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

import org.joda.time.DateTime

class PerformanceLoggingService {

    static final ThreadLocal<LoggedExecutionTimes> loggedExecutionTimesThreadLocal = new ThreadLocal<LoggedExecutionTimes>(){
        @Override
        protected LoggedExecutionTimes initialValue(){
            return new LoggedExecutionTimes()
        }
    }
    static final INDENTATION_CHAR = "-"

	enum LogLevel{
        ERROR(4),
        WARN(3),
        INFO(2),
        DEBUG(1),
        TRACE(0)

        private final Integer value
        LogLevel(Integer value){
            this.value = value
        }
        Integer getValue(){
            return this.value
        }
	}

    def logExecutionTime(LogLevel level, String description, Integer indentationDepth, Closure toMeasure) {
		DateTime started = new DateTime()
		def returnValue = toMeasure.call()
		if (level==LogLevel.ERROR) {
			log.error(getMessage(started, description, indentationDepth))
		}else if (level==LogLevel.WARN) {
			log.warn(getMessage(started, description, indentationDepth))
		}else if (level==LogLevel.INFO) {
			log.info(getMessage(started, description, indentationDepth))
		}else if (level==LogLevel.DEBUG) {
			log.debug(getMessage(started, description, indentationDepth))
		}else if (level==LogLevel.TRACE) {
			log.trace(getMessage(started, description, indentationDepth))
		}
		return returnValue
    }
    void resetExecutionTimeLoggingSession(){
        loggedExecutionTimesThreadLocal.set(new LoggedExecutionTimes())
    }
    void logExecutionTimeSilently(LogLevel level, String description, Integer indentationDepth, Closure toMeasure) {
        DateTime started = new DateTime()
        toMeasure.call()
        loggedExecutionTimesThreadLocal.get().addExecutionTime(description, indentationDepth, level, getElapsedSeconds(started))
    }
    String getExecutionTimeLoggingSessionData(LogLevel level){
        return loggedExecutionTimesThreadLocal.get().getRepresentation(level)

    }

	private String getMessage(DateTime started, String description, Integer indentationDepth){
        Double eleapsedInSeconds = getElapsedSeconds(started)
		return "${INDENTATION_CHAR*indentationDepth}${description}  -> Elapsed Sec: ${eleapsedInSeconds}"
	}

    private Double getElapsedSeconds(DateTime started) {
        Long elapsedInMillis = new DateTime().getMillis() - started.getMillis()
        Double eleapsedInSeconds = elapsedInMillis / 1000
        return eleapsedInSeconds
    }
}
