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

import de.iteratec.osm.csi.CsiValue
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.RepresentableWptResult
import grails.gorm.annotation.Entity


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
@Entity
class EventResult implements CsiValue, RepresentableWptResult {

    public static String TEST_DETAILS_STATIC_URL = "details.php?test={testid}&run={wptRun}&cached={cachedType}";

    Long id
    Date dateCreated
    Date lastUpdated
    URL testDetailsWaterfallURL
    MeasuredEvent measuredEvent
    JobGroup jobGroup
    Page page
    Browser browser
    Location location

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

    Integer consistentlyInteractiveInMillisecs
    Integer firstInteractiveInMillisecs
    Integer visuallyComplete85InMillisecs
    Integer visuallyComplete90InMillisecs
    Integer visuallyComplete95InMillisecs
    Integer visuallyComplete99InMillisecs
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

    Integer jsTotalBytes
    Integer imageTotalBytes
    Integer cssTotalBytes
    Integer htmlTotalBytes
    Integer firstMeaningfulPaint
    Integer firstContentfulPaint

    /** tester from result xml */
    String testAgent
    Integer downloadAttempts
    Date firstStatusUpdate
    Date lastStatusUpdate
    String validationState

    // from JobResult
    Date jobResultDate
    Long jobResultJobConfigId
    /**
     * This result was measured with a predefined connectivity profile.
     *
     */
    ConnectivityProfile connectivityProfile
    /**
     * If this is not null this result was measured with a connectivity configured in {@link Job}.
     *
     */
    String customConnectivityName
    /**
     * True if this result was measured without traffic shaping at all.
     */
    boolean noTrafficShapingAtAll
    /**
     * One based index of the measured step of this result within the (multistep) journey.
     */
    Integer oneBasedStepIndexInJourney

    //static belongsTo = JobResult
    static belongsTo = [jobResult: JobResult]
    static  hasMany = [userTimings : UserTiming]

    static constraints = {
        measuredEvent(nullable: false)
        jobGroup(nullable: false)
        browser(nullable: false)
        page(nullable: false)
        location(nullable: false)

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

        jsTotalBytes(nullable: true)
        imageTotalBytes(nullable: true)
        cssTotalBytes(nullable: true)
        htmlTotalBytes(nullable: true)
        firstMeaningfulPaint(nullable: true)
        firstContentfulPaint(nullable: true)

        consistentlyInteractiveInMillisecs(nullable: true)
        firstInteractiveInMillisecs(nullable: true)

        visuallyComplete85InMillisecs(nullable: true)
        visuallyComplete90InMillisecs(nullable: true)
        visuallyComplete95InMillisecs(nullable: true)
        visuallyComplete99InMillisecs(nullable: true)

        downloadAttempts(nullable: true)
        firstStatusUpdate(nullable: true)
        lastStatusUpdate(nullable: true)
        validationState(nullable: true)

        // from JobResult
        jobResultDate(nullable: false)
        jobResultJobConfigId(nullable: false)

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
        oneBasedStepIndexInJourney(nullable: true)
        userTimings(nullable: true)
    }

    static mapping = {

        jobResultDate(index: 'jobResultDate_and_jobResultJobConfigId_idx,wJRD_and_wJRJCId_and_mV_and_cV_idx,GetLimitedMedianEventResultsBy,forEventResultDashboard')
        jobResultJobConfigId(index: 'jobResultDate_and_jobResultJobConfigId_idx,wJRD_and_wJRJCId_and_mV_and_cV_idx')
        medianValue(index: 'wJRD_and_wJRJCId_and_mV_and_cV_idx')
        cachedView(index: 'wJRD_and_wJRJCId_and_mV_and_cV_idx')
        jobGroup(index: 'forEventResultDashboard')
        page(index: 'forEventResultDashboard')
        connectivityProfile(index: 'forEventResultDashboard')
        fullyLoadedTimeInMillisecs(index: 'forEventResultDashboard')

        noTrafficShapingAtAll(defaultValue: false)
    }

    static transients = ['csiRelevant']

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

    @Override
    JobGroup retrieveJobGroup() {
        return jobGroup
    }

    @Override
    Page retrievePage() {
        return page
    }

    @Override
    Browser retrieveBrowser() {
        return browser
    }
/**
     * <p>
     * Build up an URL to display details of the given {@link EventResult}s.
     * </p>
     * @param jobRun
     * 			The associated {@link JobResult} of the given {@link EventResult}s
     * @return The created URL <code>null</code> if not possible to build up an URL
     */
    URL buildTestDetailsURL(JobResult jobRun, String waterfallAnchor) {
        URL resultURL = null
        String urlString = null

        if (jobRun) {
            urlString = jobRun.getWptServerBaseurl() + TEST_DETAILS_STATIC_URL + waterfallAnchor
            urlString = urlString.replace("{testid}", jobRun.getTestId())
            urlString = urlString.replace("{wptRun}", this.numberOfWptRun.toString())
            urlString = urlString.replace("{cachedType}", (this.cachedView.toString() == "CACHED" ? "1" : "0"))
            resultURL = new URL(urlString)
        }

        return resultURL
    }

    String toString() {
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
                "\t\tjobResultJobConfigId=${this.jobResultJobConfigId}\n"
    }

}
