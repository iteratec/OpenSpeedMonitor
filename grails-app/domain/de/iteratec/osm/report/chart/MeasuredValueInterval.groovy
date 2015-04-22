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

package de.iteratec.osm.report.chart

/**
 * 
 * @TODO TODO mze-2013-07-12: Questions:
 *   - Why this is not an enum? Are there are more possible values? 
 *   - Why there are constants an table data rows? 
 *   - Why the constants are int-values and not MeasuredValueInterval instances?
 *   - Name is a bit cryptic - This seems to be a time or date range.
 */
class MeasuredValueInterval {
	
	public static final int RAW = -1
	public static final int HOURLY = 60
	public static final int DAILY = 24 * HOURLY
	public static final int WEEKLY = 7 * DAILY
	
	/**
	 * @TODO TODO mze-2013-07-12: Questions: Is this a logical or a name for the UI? If for UI: Why this is stored in the database? If a logical one: Syntax? Semantics? Purposes?
	 */
	String name
	
	Integer intervalInMinutes
	
	static contraints = {
		intervalInMinutes(unique: true)
	}
	static mapping = {
		cache usage: 'nonstrict-read-write'
	}

	public String toString(){
		return name
	}
}
