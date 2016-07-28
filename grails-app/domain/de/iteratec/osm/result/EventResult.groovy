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

import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.CsiValue


/**
 * The cache-setting a {@link EventResult result} is based on.
 * 
 * @author nkuhn
 */
public enum CachedView {
	/**
	 * A WPT repeated view result; could have used some data out of the 
	 * browsers cache.
	 */
	CACHED,
	
	/**
	 * A WPT first view result; the browsers cache have been cleared before, 
	 * all date was loaded from the web-server.
	 */
	UNCACHED;
	
	static transients = ['cached'];
	
	/**
	 * <p>
	 * Determines weather this cached-view-state is a cached state (repeated 
	 * view) or an un-cached state (first view).
	 * </p>
	 * 
	 * @return <code>true</code> if this is a cached state, 
	 *         <code>false</code> else.
	 */
	public boolean isCached()
	{
		return this == CACHED;
	}
}

/**
 * <p>
 * The results of one event in one and only one 
 * {@linkplain JobResult job-result}.
 * </p>
 * 
 * <p>
 * For job-results in WPT-multistep this is the result of one page-load.
 * It is assigned to one and only one job-result
 * and grouped by a {@link MeasuredEvent}.
 * An event-result could be a cached (repeated view) or un-cached (first view)
 * execution (see {#cachedView}).
 * </p>
 * 
 * @author nkuhn
 * @author mze
 * @see JobResult
 */
class EventResult implements CsiValue {

	public static String TEST_DETAILS_STATIC_URL = "details.php?test={testid}&run={wptRun}&cached={cachedType}";

	OsmConfigCacheService osmConfigCacheService

	Long id
	Date dateCreated
	Date lastUpdated
	URL testDetailsWaterfallURL

	MeasuredEvent measuredEvent

	/** number of the run in wpt */
	Integer numberOfWptRun
	CachedView cachedView
	/** whether this result's doc-ready-time is the median time of all runs (if this jobrun includes just one run allways true) */
	Boolean medianValue
	Integer wptStatus

	Integer docCompleteIncomingBytes
	Integer docCompleteRequests
	Integer docCompleteTimeInMillisecs
	Integer domTimeInMillisecs
	Integer firstByteInMillisecs
	Integer fullyLoadedIncomingBytes
	Integer fullyLoadedRequestCount
	Integer fullyLoadedTimeInMillisecs
	Integer loadTimeInMillisecs
	Integer startRenderInMillisecs
	Double csByWptDocCompleteInPercent
	Double csByWptVisuallyCompleteInPercent

	/**
	 * The WPT speed index received from WPT server.
	 *
	 * If value is not available please assign 
	 * {@link #SPEED_INDEX_DEFAULT_VALUE}.
	 *
	 * Never <code>null</code>.
	 */
	Integer speedIndex
	Integer visuallyCompleteInMillisecs
    /** tester from result xml */
    String testAgent

	/**
	 * This is value is to be used for {@link #speedIndex} if no value is
	 * available.
	 *
	 * @since IT-223
	 */
	public static final Integer SPEED_INDEX_DEFAULT_VALUE = -1;

	Integer downloadAttempts
	Date firstStatusUpdate
	Date lastStatusUpdate
	String validationState
	String tag

	// from JobResult 
	Date jobResultDate
	Long jobResultJobConfigId

	/**
	 * This result was measured with a predefined connectivity profile.
	 *
	 */
	ConnectivityProfile connectivityProfile;

    /**
     * If this is not null this result was measured with a connectivity configured in {@link Job}.
     *
     */
	String customConnectivityName;

    /**
     * True if this result was measured without traffic shaping at all.
     */
    boolean noTrafficShapingAtAll


	//static belongsTo = JobResult
	static belongsTo = [jobResult: JobResult]

	static constraints = {
//		id()
		measuredEvent(nullable: true) // FIXME mze-2013-07-30: CAHNGE IMMEDIATELLY to never be null!
		wptStatus(nullable: false)
		medianValue(nullable: false)
		numberOfWptRun(nullable: false)
		cachedView(nullable: false)
		testDetailsWaterfallURL(nullable: true)

		docCompleteIncomingBytes(nullable: true)
		docCompleteRequests(nullable: true)
		docCompleteTimeInMillisecs(nullable: true)
		domTimeInMillisecs(nullable: true)
		firstByteInMillisecs(nullable: true)
		fullyLoadedIncomingBytes(nullable: true)
		fullyLoadedRequestCount(nullable: true)
		fullyLoadedTimeInMillisecs(nullable: true)
		loadTimeInMillisecs(nullable: true)
		startRenderInMillisecs(nullable: true)
		csByWptDocCompleteInPercent(nullable: true)
		csByWptVisuallyCompleteInPercent(nullable: true)
		speedIndex(nullable: true)
		visuallyCompleteInMillisecs(nullable: true)

		downloadAttempts(nullable: true)
		firstStatusUpdate(nullable: true)
		lastStatusUpdate(nullable: true)
		validationState(nullable: true)

		// from JobResult
		jobResultDate(nullable: false)
		jobResultJobConfigId(nullable: false)

		tag(nullable: true)

        testAgent(nullable: true)

		connectivityProfile(nullable: true, validator: { currentProfile, eventResultInstance ->

            boolean notNullAndNothingElse =
                    currentProfile != null &&
                        eventResultInstance.customConnectivityName == null &&
                        eventResultInstance.noTrafficShapingAtAll == false
            boolean nullAndCustom =
                    currentProfile == null &&
                            eventResultInstance.customConnectivityName != null &&
                            eventResultInstance.noTrafficShapingAtAll == false
            boolean nullAndNative =
                    currentProfile == null &&
                            eventResultInstance.customConnectivityName == null &&
                            eventResultInstance.noTrafficShapingAtAll == true

            return notNullAndNothingElse || nullAndCustom || nullAndNative;

        })
		customConnectivityName(nullable: true, validator: { currentCustomName, eventResultInstance ->

            boolean notNullAndNothingElse =
                    currentCustomName != null &&
                            eventResultInstance.noTrafficShapingAtAll == false &&
                            eventResultInstance.connectivityProfile == null
            boolean nullAndNative =
                    currentCustomName == null &&
                            eventResultInstance.noTrafficShapingAtAll == true &&
                            eventResultInstance.connectivityProfile == null
            boolean nullAndPredefined =
                    currentCustomName == null &&
                            eventResultInstance.noTrafficShapingAtAll == false &&
                            eventResultInstance.connectivityProfile != null

            return notNullAndNothingElse || nullAndNative || nullAndPredefined

        })
	}

	static mapping = {

		jobResultDate(index: 'jobResultDate_and_jobResultJobConfigId_idx,wJRD_and_wJRJCId_and_mV_and_cV_idx,GetLimitedMedianEventResultsBy')
		jobResultJobConfigId(index: 'jobResultDate_and_jobResultJobConfigId_idx,wJRD_and_wJRJCId_and_mV_and_cV_idx')
		medianValue(index: 'wJRD_and_wJRJCId_and_mV_and_cV_idx')
		cachedView(index: 'wJRD_and_wJRJCId_and_mV_and_cV_idx')

        noTrafficShapingAtAll(defaultValue: false)

	}

	static transients = ['csiRelevant', 'osmConfigCacheService']

	@Override
	public Double retrieveCsByWptDocCompleteInPercent() {
		return csByWptDocCompleteInPercent
	}

	@Override
	public Double retrieveCsByWptVisuallyCompleteInPercent() {
		return csByWptVisuallyCompleteInPercent
	}

	@Override
	Date retrieveDate() {
		return this.jobResultDate
	}

	@Override
	public String retrieveTag() {
		return this.tag
	}

	@Override
	ConnectivityProfile retrieveConnectivityProfile() {
		return this.connectivityProfile
	}

	@Override
	public List<Long> retrieveUnderlyingEventResultsByDocComplete() {
		return [this.id]
	}

	@Override
	public List<EventResult> retrieveUnderlyingEventResultsByVisuallyComplete() {
		return [this]
	}

	/**
	 * <p>
	 * Build up an URL to display details of the given {@link EventResult}s.
	 * </p>
	 * @param jobRun
	 * 			The associated {@link JobResult} of the given {@link EventResult}s
	 * @return The created URL <code>null</code> if not possible to build up an URL
	 */
	public URL buildTestDetailsURL(JobResult jobRun, String waterfallAnchor) {
		URL resultURL = null;
		String urlString = null;

		if (jobRun) {
			urlString = jobRun.getWptServerBaseurl() + TEST_DETAILS_STATIC_URL + waterfallAnchor
			urlString = urlString.replace("{testid}", jobRun.getTestId());
			urlString = urlString.replace("{wptRun}", this.numberOfWptRun.toString());
			urlString = urlString.replace("{cachedType}", (this.cachedView.toString() == "CACHED" ? "1" : "0"));
			resultURL = new URL(urlString);
		}

		return resultURL;
	}

	public String toString() {
		return "id=${this.id}\n" +
				"\t\twptStatus=${this.wptStatus}\n" +
				"\t\tmedianValue=${this.medianValue}\n" +
				"\t\tnumberOfWptRun=${this.numberOfWptRun}\n" +
				"\t\tcachedView=${this.cachedView}\n" +
				"\t\tdocCompleteIncomingBytes=${this.docCompleteIncomingBytes}\n" +
				"\t\tdocCompleteRequests=${this.docCompleteRequests}\n" +
				"\t\tdocCompleteTimeInMillisecs=${this.docCompleteTimeInMillisecs}\n" +
				"\t\tdomTimeInMillisecs=${this.domTimeInMillisecs}\n" +
				"\t\tfirstByteInMillisecs=${this.firstByteInMillisecs}\n" +
				"\t\tfullyLoadedIncomingBytes=${this.fullyLoadedIncomingBytes}\n" +
				"\t\tfullyLoadedRequestCount=${this.fullyLoadedRequestCount}\n" +
				"\t\tfullyLoadedTimeInMillisecs=${this.fullyLoadedTimeInMillisecs}\n" +
				"\t\tloadTimeInMillisecs=${this.loadTimeInMillisecs}\n" +
				"\t\tstartRenderInMillisecs=${this.startRenderInMillisecs}\n" +
				"\t\tcustomerSatisfactionInPercent=${this.csByWptDocCompleteInPercent}\n" +
				"\t\tspeedIndex=${this.speedIndex}\n" +
				"\t\tdownloadAttempts=${this.downloadAttempts}\n" +
				"\t\tfirstStatusUpdate=${this.firstStatusUpdate}\n" +
				"\t\tlastStatusUpdate=${this.lastStatusUpdate}\n" +
				"\t\tvalidationState=${this.validationState}\n" +
				"\t\tjobResultDate=${this.jobResultDate}\n" +
				"\t\tjobResultJobConfigId=${this.jobResultJobConfigId}\n" +
				"\t\ttag=${this.tag}"
	}

}
