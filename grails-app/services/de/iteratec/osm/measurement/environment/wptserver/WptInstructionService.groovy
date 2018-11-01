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

import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.PerformanceLoggingService
import grails.async.Promise
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.NativeHandlers

import java.util.concurrent.locks.ReentrantLock

import static grails.async.Promises.task

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
    Object runtest(WebPageTestServer wptserver, Map params) {
        Map paramsNotNull = params.findAll {k,v-> v != null  }
        return httpRequestService.getRestClientFrom(wptserver).post {
            request.uri.path = '/runtest.php'
            request.body = paramsNotNull
            request.contentType = 'application/x-www-form-urlencoded'
            request.encoder('application/x-www-form-urlencoded', NativeHandlers.Encoders.&form)
            request.headers['Accept'] = 'application/xml'
        }
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
        Integer statusCode = resultXml.statusCodeOfWholeTest
        def state = [0: 'Failure!', 100: 'Test Pending', 101: 'Test Started', 200: 'Test Finished']
        log.info("Result-Status of ${resultId}: ${statusCode} (${state[statusCode]})")

        boolean resultXmlHasRuns = resultXml.hasRuns()
        Integer runCount = resultXmlHasRuns ? resultXml.runCount : null

        log.info("xmlResultResponse.data.runs.toString().isInteger()=${resultXmlHasRuns}|")
        log.info("xmlResultResponse.data.runs.sizeRuns=${runCount}")


        if (statusCode >= 200 && resultXmlHasRuns) {
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

}
