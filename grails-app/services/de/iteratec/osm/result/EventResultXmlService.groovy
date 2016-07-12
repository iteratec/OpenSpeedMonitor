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

import grails.transaction.Transactional
import groovy.util.slurpersupport.GPathResult

@Transactional
class EventResultXmlService {

	/**
	 * Gets count of teststeps in given xmlResult.
	 * @param xmlResultResponse Should be whole result-xml from root-(<response>)tag.
	 * @return Number of teststeps
	 */
    Integer getTeststepCount(GPathResult xmlResultResponse){
		Integer testStepCount = 0
		
		//More than one testStep, if testStep-Notation isn't empty
		if (!xmlResultResponse.data.median.firstView.testStep.isEmpty()) {
			testStepCount=xmlResultResponse.data.median.firstView.testStep.size();
			
		} else if(!xmlResultResponse.data.median.firstView.isEmpty()) { // testStep isempty == old data == 1step
			testStepCount=1;
		}
		
		return testStepCount
	}
	
}
