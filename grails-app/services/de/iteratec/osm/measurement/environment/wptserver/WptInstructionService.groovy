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

package de.iteratec.osm.measurement.environment.wptserver

import de.iteratec.osm.ConfigService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.WptStatus
import de.iteratec.osm.util.PerformanceLoggingService
import grails.async.Promise
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import groovyx.net.http.NativeHandlers

import java.util.concurrent.locks.ReentrantLock

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG
import static grails.async.Promises.task

class JobExecutionException extends Exception {
    WptStatus wptStatus
    String testId

    JobExecutionException(String message, WptStatus wptStatus, String testId) {
        super(message)
        this.wptStatus = wptStatus
        this.testId = testId
    }

}

interface iResultListener {
    public String getListenerName()

    public void listenToResult(
            WptResultXml resultXml,
            WebPageTestServer wptserver,
            long jobId
    )

    public boolean callListenerAsync()
}

interface iLocationListener {
    public String getListenerName()

    public List<Location> listenToLocations(GPathResult result, WebPageTestServer wptserver)
}

/**
 * Business logic to control WebpageTest server via http(s).
 *
 * Observers can register with {@link WptInstructionService#addResultListener(de.iteratec.osm.measurement.environment.wptserver.iResultListener)}
 *      to get informed about new successful wpt results.
 * Observers can register with {@link WptInstructionService#addLocationListener(de.iteratec.osm.measurement.environment.wptserver.iLocationListener)}
 *      to get informed about new successful wpt locations.

 * @author rschuett , nkuhn
 */
class WptInstructionService {

    protected List<iResultListener> resultListeners = new ArrayList<iResultListener>()
    protected List<iLocationListener> locationListeners = new ArrayList<iLocationListener>()
    private final ReentrantLock lock = new ReentrantLock()

    HttpRequestService httpRequestService
    PerformanceLoggingService performanceLoggingService
    ConfigService configService

    /**
     * Listeners can register as oberservers.
     * @param listener
     */
    void addResultListener(iResultListener listener) {
        this.resultListeners.add(listener)
    }

    void addLocationListener(iLocationListener listener) {
        this.locationListeners.add(listener)
    }

    /**
     * Runs test against given wptserver.
     * @param wptserver
     * 			Instance of PHP-application webpagetest (see http://webpagetest.org).
     * @param params
     * 			Should contain all necessary for running tests on wptserver.
     * @return
     */
    String runtest(Job job, int priority) {
        WebPageTestServer wptServer = job?.location?.wptServer
        if (!wptServer) {
            throw new JobExecutionException("Missing wptServer in job ${job.label}", WptStatus.UNKNOWN, null)
        }

        def parameters = fillRequestParameters(job)
        parameters['priority'] = priority
        Map paramsNotNull = parameters.findAll { k, v -> v != null }
        def result = null
        performanceLoggingService.logExecutionTime(DEBUG, "Launching job ${job.label}: Calling initial runtest on wptserver.", 1) {
            result = httpRequestService.getRestClientFrom(wptServer).post {
                request.uri.path = '/runtest.php'
                request.body = paramsNotNull
                request.contentType = 'application/x-www-form-urlencoded'
                request.encoder('application/x-www-form-urlencoded', NativeHandlers.Encoders.&form)
                request.headers['Accept'] = 'application/xml'
            }
        }

        NodeChild runtestResponseXml = parseResponse(result, wptServer)
        WptStatus wptStatus = WptStatus.byResultCode(runtestResponseXml?.statusCode?.toInteger())
        String testId = runtestResponseXml?.data?.testId
        if (wptStatus != WptStatus.COMPLETED) {
            throw new JobExecutionException("Got status code ${wptStatus} from wptserver ${wptServer}", wptStatus, testId)
        }
        if (!testId) {
            throw new JobExecutionException("Jobrun failed for: wptserver=${wptServer}, sent params=${parameters} => got no testId in response", wptStatus, testId)
        }
        log.info("Jobrun successfully launched: wptserver=${wptServer}, sent params=${parameters}, got testID: ${testId}")
        return testId
    }

    Object cancelTest(WebPageTestServer wptserver, Map params) {
        return httpRequestService.getRestClientFrom(wptserver).post{
            request.uri.path = '/cancelTest.php'
            request.uri.query = params
            request.contentType = 'text/plain'
        }
    }

    /**
     * Gets locations from given wptserver.
     * @param wptserver
     * 			Instance of PHP-application webpagetest (see http://webpagetest.org).
     */
    List<Location> fetchLocations(WebPageTestServer wptserver) {
        return fetchLocations(wptserver, [:])
    }

    /**
     * Gets locations from given wptserver.
     * @param wptServer
     *          Instance of PHP-application webpagetest (see http://webpagetest.org).
     * @param queryParams
     *          Query parameters to send to getLocations.php of webpagetest server.
     * @return List of fetched locations.
     */
    List<Location> fetchLocations(WebPageTestServer wptserver, Map queryParams){
        List<Location> addedLocations = []

        def locationsResponse = httpRequestService.getWptServerHttpGetResponse(
            wptserver,
            '/getLocations.php',
            queryParams,
                'application/xml',
            [Accept: 'application/xml']
        )

        log.info("${this.locationListeners.size} iResultListener(s) listen to the fetching of locations")
        this.locationListeners.each {
            log.info("calling listenToLocations for iLocationListener ${it.getListenerName()}")
            addedLocations.addAll(it.listenToLocations(locationsResponse, wptserver))
        }

        return addedLocations
    }

    /**
     * Gets result from given wptserver via REST-call.
     * @param wptserverOfResult
     * 			Instance of PHP-application webpagetest (see http://webpagetest.org) from which xml-result should be get.
     * @param resultId
     * 			Id of webpagetest result
     * @param job
     * 	        Job that initiated webpagetest
     * @return
     */
    WptResultXml fetchResult(WebPageTestServer wptserverOfResult, String resultId, Job job) {
        log.info("Start Saving result ${wptserverOfResult.baseUrl}result/${resultId}")

        GPathResult xmlResultResponse = getXmlResult(wptserverOfResult, resultId)
        WptResultXml resultXml = convertGPathToWptResultXML(xmlResultResponse)
        WptStatus wptStatus = resultXml.wptStatus

        log.info("Result-Status of ${resultId}: ${wptStatus.toString()} (${wptStatus.wptStatusCode})")
        log.info("resultXml.hasRuns()=${resultXml.hasRuns()}|")
        log.info("resultXml.runCount=${resultXml.hasRuns() ? resultXml.runCount : null}")


        if (resultXml.isFinishedWithResults()) {
            try {
                lock.lockInterruptibly()
                this.resultListeners.each { listener ->
                    log.info("calling listener ${listener.listenerName} for job id ${job.id}")
                    if (listener.callListenerAsync()) {
                        Promise p = task {
                            JobResult.withNewSession {
                                listener.listenToResult(resultXml, wptserverOfResult, job.id)
                            }
                        }
                        p.onError { Throwable err -> log.error("${listener.getListenerName()} failed persisting results", err) }
                        p.onComplete { log.info("${listener.getListenerName()} successfully returned from async task") }
                    } else {
                        listener.listenToResult(resultXml, wptserverOfResult, job.id)
                    }
                }

            } finally {
                lock.unlock()
            }

        }

        return resultXml

    }

    private WptResultXml convertGPathToWptResultXML(GPathResult xmlResultResponse) {
        WptResultXml resultXml = new WptResultXml(xmlResultResponse)
        return resultXml
    }

    private GPathResult getXmlResult(WebPageTestServer wptserverOfResult, String resultId) {
        return httpRequestService.getWptServerHttpGetResponse(
            wptserverOfResult,
            '/xmlResult.php',
            ['f': 'xml', 'test': resultId, 'r': resultId, 'multistepFormat': '1'],
                'application/xml',
            [Accept: 'application/xml']
        )
    }

    /**
     * Maps the properties of a Job to the parameters expected by
     * the REST API available at https://sites.google.com/a/webpagetest.org/docs/advanced-features/webpagetest-restful-apis
     *
     * @return A map of parameters suitable for POSTing a valid request to runtest.php
     */
    private Map fillRequestParameters(Job job) {
        Map parameters = [
                url            : '',
                label          : job.label,
                location       : job.location.uniqueIdentifierForServer,
                runs           : job.runs,
                fvonly         : job.firstViewOnly,
                video          : job.captureVideo,
                web10          : job.web10,
                noscript       : job.noscript,
                clearcerts     : job.clearcerts,
                ignoreSSL      : job.ignoreSSL,
                standards      : job.standards,
                tcpdump        : job.tcpdump,
                continuousVideo: job.continuousVideo,
                private        : job.isPrivate,
                block          : job.urlsToBlock,
                mobile         : job.emulateMobile,
                dpr            : job.devicePixelRation,
                cmdline        : job.cmdlineOptions,
                custom         : job.customMetrics,
                tester         : job.tester,
                timeline       : job.captureTimeline,
                timelineStack  : job.javascriptCallstack,
                mobileDevice   : job.mobileDevice,
                lighthouse     : job.performLighthouseTest,
                type           : job.optionalTestTypes,
                customHeaders  : job.customHeaders,
                trace          : job.trace,
                traceCategories: job.traceCategories,
                spof           : job.spof
        ]
        if (job.takeScreenshots == Job.TakeScreenshots.NONE) {
            parameters.noimages = true
        } else if (job.takeScreenshots == Job.TakeScreenshots.FULL) {
            parameters.pngss = true
        } else {
            parameters.iq = job.imageQuality
        }

        if (job.saveBodies == Job.SaveBodies.HTML) {
            parameters.htmlbody = true
        } else if (job.saveBodies == Job.SaveBodies.ALL) {
            parameters.bodies = true
        }

        if (configService.globalUserAgentSuffix && job.useGlobalUASuffix) {
            parameters.appendua = configService.globalUserAgentSuffix
        } else {
            if (job.userAgent == Job.UserAgent.ORIGINAL) {
                parameters.keepua = true
            } else if (job.userAgent == Job.UserAgent.APPEND) {
                parameters.appendua = job.appendUserAgent
            } else if (job.userAgent == Job.UserAgent.OVERWRITE) {
                parameters.uastring = job.userAgentString
            }
        }


        if (job.noTrafficShapingAtAll) {
            parameters.location += ".Native"
        } else {
            parameters.location += ".custom"
            if (job.customConnectivityProfile) {
                parameters.bwDown = job.bandwidthDown
                parameters.bwUp = job.bandwidthUp
                parameters.latency = job.latency
                parameters.plr = job.packetLoss
            } else {
                parameters.bwDown = job.connectivityProfile.bandwidthDown
                parameters.bwUp = job.connectivityProfile.bandwidthUp
                parameters.latency = job.connectivityProfile.latency
                parameters.plr = job.connectivityProfile.packetLoss
            }
        }

        if (job.script) {
            parameters.script = job.script.getParsedNavigationScript(job)
            if (job.provideAuthenticateInformation) {
                parameters.login = job.authUsername
                parameters.password = job.authPassword
            }
        }

        String apiKey = job.location.wptServer.apiKey
        if (apiKey) {
            parameters.k = apiKey
        }

        parameters.f = 'xml'

        // convert all boolean parameters to 0 or 1
        parameters = parameters.each {
            if (it.value instanceof Boolean && it.value != null)
                it.value = it.value ? 1 : 0
        }
        return parameters
    }


    private NodeChild parseResponse(def runtestResponse, WebPageTestServer wptserver) {
        NodeChild runtestResponseXml
        if (runtestResponse instanceof NodeChild || runtestResponse instanceof GPathResult) {
            runtestResponseXml = runtestResponse
            return runtestResponseXml
        } else {
            throw new JobExecutionException("Response is not XML from wptserver ${wptserver}", WptStatus.UNKNOWN, null)
        }
    }
}
