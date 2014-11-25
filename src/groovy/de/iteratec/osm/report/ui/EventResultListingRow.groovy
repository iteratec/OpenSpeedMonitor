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

package de.iteratec.osm.report.ui

import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent

/**
 * <p>
 * Visualization of one {@link EventResult} as one row in an event 
 * result listing table.
 * </p>
 * 
 * <p>
 * Objects of this class intended to be unmodifiable. 
 * </p>
 * 
 * @author mze
 * 
 * @since IT-106 (Extracted from {@link EventResultController} 
 *         since 8th October 2013) 
 */
public class EventResultListingRow {

	/**
	 * <p>
	 * Creates a new row.
	 * </p>
	 * 
	 * @param jobResult
	 *         The {@linkplain JobResult job result} to list in this row.
	 * @param eventResult 
	 *         An {@linkplain EventResult event result} of the passed 
	 *         {@linkplain JobResult job result}. This event result should
	 *         be contained in {@linkplain JobResult#getEventResults() 
	 *         jobResult.getEventResults()} otherwise the result is 
	 *         undefined. 
	 */
	public EventResultListingRow(JobResult jobResult, EventResult eventResult) {
		scheduled = jobResult.date;
		jobName = jobResult.jobConfigLabel

		MeasuredEvent measuredEvent = eventResult.measuredEvent;
		measuredEventName= measuredEvent.name;
		pageName= measuredEvent.testedPage.name;

		label = jobResult.jobConfigLabel;
		testsDetailsURL = eventResult.getTestDetailsWaterfallURL() != null ? eventResult.getTestDetailsWaterfallURL() : jobResult.tryToGetTestsDetailsURL();
		
		measuringDate = jobResult.date;

		timeToFirstByteInMillis = eventResult.firstByteInMillisecs;
		startToRenderInMillis = eventResult.startRenderInMillisecs;
		domTimeInMillis = eventResult.domTimeInMillisecs;

		docCompleteIncomingBytes = eventResult.docCompleteIncomingBytes;
		docCompleteRequests = eventResult.docCompleteRequests;
		docCompleteTimeInMillis = eventResult.docCompleteTimeInMillisecs;

		fullyLoadedIncomingBytes = eventResult.fullyLoadedIncomingBytes;
		fullyLoadedRequestCount = eventResult.fullyLoadedRequestCount;
		fullyLoadedTimeInMillis = eventResult.fullyLoadedTimeInMillisecs;

		firstView = ! eventResult.getCachedView().isCached();
	}

	public final String label;

	public final Date measuringDate;

	public final String jobName;
	public final String measuredEventName;
	public final String pageName;

	public final Date scheduled;

	public final URL testsDetailsURL;

	public final Integer timeToFirstByteInMillis

	public final Integer startToRenderInMillis

	public final Integer domTimeInMillis

	public final Integer docCompleteIncomingBytes

	public final Integer docCompleteRequests

	public final Integer docCompleteTimeInMillis

	public final Integer fullyLoadedIncomingBytes

	public final Integer fullyLoadedRequestCount

	public final Integer fullyLoadedTimeInMillis

	public final Boolean firstView;
}
