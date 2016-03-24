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

    static transactional = false
	
	enum LogLevel{
		FATAL, ERROR, WARN, INFO, DEBUG, TRACE
	}
	enum IndentationDepth {
		singleIndentationChar('-'),
		NULL (''),
	    ONE (" ${singleIndentationChar.prefix*1}"),
	    TWO (" ${singleIndentationChar.prefix*2}"),
	    THREE (" ${singleIndentationChar.prefix*3}"),
		FOUR (" ${singleIndentationChar.prefix*4}"),
		FIVE (" ${singleIndentationChar.prefix*5}")
	
	    private final String prefix
	    IndentationDepth(String prefix) {
	        this.prefix = prefix
	    }
	}

    def logExecutionTime(LogLevel level, String description, IndentationDepth indentation, Closure toMeasure) {
		DateTime started = new DateTime()
		toMeasure.call()
		if (level==LogLevel.FATAL && log.fatalEnabled) {
			log.fatal(getMessage(started, description, indentation))
		}else if (level==LogLevel.ERROR && log.errorEnabled) {
			log.error(getMessage(started, description, indentation))
		}else if (level==LogLevel.WARN && log.warnEnabled) {
			log.warn(getMessage(started, description, indentation))
		}else if (level==LogLevel.INFO && log.infoEnabled) {
			log.info(getMessage(started, description, indentation))
		}else if (level==LogLevel.DEBUG && log.debugEnabled) {
			log.debug(getMessage(started, description, indentation))
		}else if (level==LogLevel.TRACE && log.traceEnabled) {
			log.trace(getMessage(started, description, indentation))
		}
    }
	private String getMessage(DateTime started, String description, IndentationDepth indentation){
		def elapsedInMillis = new DateTime().getMillis() - started.getMillis()
		def eleapsedInSeconds = elapsedInMillis / 1000
		return "${indentation.prefix}${description}  -> Elapsed Sec: ${eleapsedInSeconds}"
	}
}
