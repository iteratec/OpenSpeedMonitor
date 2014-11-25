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

package de.iteratec.osm.csi

import org.joda.time.DateTime
import org.joda.time.Duration


/**
 * StraightLine representing a target-customer satisfaction.
 * @author nkuhn
 * @see CsTargetValue
 *
 */
class CsTargetGraph {
	
	String label
	String description
	
	CsTargetValue pointOne
	CsTargetValue pointTwo
	
	/** Whether or not to show the graph first loading the containing diagram. */
	Boolean defaultVisibility
	
	static transients = {'percentOfDate'}
	
	/** Calculates csInPercent for given dateTime by using the function defined by the two points {@link #pointOne} and {@link #pointTwo}. */
	public Double getPercentOfDate(DateTime dateTime){
		Double deltaDateBetweenPointsInMillis = new Duration(new DateTime(pointOne.date).getMillis(), new DateTime(pointTwo.date).getMillis()).getStandardSeconds()*1000
		Double deltaCsPercentBetweenPoints = Math.abs(pointOne.csInPercent - pointTwo.csInPercent)
		Double steigung = deltaCsPercentBetweenPoints / deltaDateBetweenPointsInMillis
		Double yAchsenabschnitt = pointOne.csInPercent - steigung * new DateTime(pointOne.date).getMillis()
		return yAchsenabschnitt + steigung * dateTime.getMillis()
	}
	
    static constraints = {
		label()
		description(nullable: true, widget: 'textarea')
		pointOne()
		pointTwo()
		defaultVisibility()
    }
	
	static mapping = {
		description(type: 'text')
		defaultVisibility(defaultValue: false)
	}
}
