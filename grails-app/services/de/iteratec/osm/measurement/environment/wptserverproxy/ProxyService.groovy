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

package de.iteratec.osm.measurement.environment.wptserverproxy

import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.PerformanceLoggingService
import grails.async.Promise
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator

import java.util.concurrent.locks.ReentrantLock

import static grails.async.Promises.task

interface iResultListener {
    public String getListenerName()

    public void listenToResult(
            WptResultXml resultXml,
            WebPageTestServer wptserver
    )

    public boolean callListenerAsync()
}

interface iLocationListener {
    public String getListenerName()

    public List<Location> listenToLocations(GPathResult result, WebPageTestServer wptserver)
}

//TODO: Write further tests for this service. Recording of http-responses is necessary!
//				See the following for that:
//				* http://freeside.co/betamax/
//				* https://github.com/robfletcher/betamax/tree/master/examples/grails-betamax 

/**
 * Business logic for functionality of wptserver-proxy. Observers can register with {@link ProxyService#addResultListener(iResultListener)}.
 * @author rschuett , nkuhn
 */
class ProxyService {

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
    HttpResponseDecorator runtest(WebPageTestServer wptserver, Map params) {
        log.info("baseurl of called wptsever=${wptserver.baseUrl}")


        return httpRequestService.getRestClientFrom(wptserver).post(
                path: 'runtest.php',
                query: params,
                contentType: ContentType.TEXT,
                headers: [Accept: 'application/xml']
        )

    }

    HttpResponseDecorator cancelTest(WebPageTestServer wptserver, Map params) {
        return httpRequestService.getRestClientFrom(wptserver).post(
                path: 'cancelTest.php',
                query: params,
                contentType: ContentType.TEXT
        )
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
    List<Location> fetchLocations(WebPageTestServer wptServer, Map queryParams){
        List<Location> addedLocations = []

        def locationsResponse = httpRequestService.getWptServerHttpGetResponseAsGPathResult(wptserver, 'getLocations.php', queryParams, ContentType.TEXT, [Accept: 'application/xml'])

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
     * @param params
     * 			Must contain resultId.
     * @return
     */
    WptResultXml fetchResult(WebPageTestServer wptserverOfResult, Map params) {
        log.info("Start Saving result ${wptserverOfResult.baseUrl}result/${params.resultId}")

        GPathResult xmlResultResponse = getXmlResult(wptserverOfResult, params)
        WptResultXml resultXml = convertGPathToWptResultXML(xmlResultResponse)
        Integer statusCode = resultXml.statusCodeOfWholeTest
        def state = [0: 'Failure!', 100: 'Test Pending', 101: 'Test Started', 200: 'Test Finished']
        log.info("Result-Status of ${params.resultId}: ${statusCode} (${state[statusCode]})")

        final String jobLabel = resultXml.getLabel()
        boolean resultXmlHasRuns = resultXml.hasRuns()
        Integer runCount = resultXmlHasRuns ? resultXml.runCount : null

        log.info("xmlResultResponse.data.runs.toString().isInteger()=${resultXmlHasRuns}|")
        log.info("xmlResultResponse.data.runs.sizeRuns=${runCount}")


        if (jobLabel.length() > 0 && statusCode >= 200 && resultXmlHasRuns) {
            try {

                lock.lockInterruptibly();
                this.resultListeners.each { listener ->
                    if (listener.callListenerAsync()) {
                        Promise p = task {
                            JobResult.withNewSession {
                                listener.listenToResult(resultXml, wptserverOfResult)
                            }
                        }
                        p.onError { Throwable err -> log.error(err) }
                        p.onComplete { log.info("${listener.getListenerName()} successfully returned from async task") }
                    } else {
                        listener.listenToResult(resultXml, wptserverOfResult)
                    }
                }

            } finally {
                lock.unlock();
            }

        }

        return resultXml

    }

    private WptResultXml convertGPathToWptResultXML(GPathResult xmlResultResponse) {
        WptResultXml resultXml = new WptResultXml(xmlResultResponse)
        return resultXml
    }

    private GPathResult getXmlResult(WebPageTestServer wptserverOfResult, Map params) {
        return httpRequestService.getWptServerHttpGetResponseAsGPathResult(wptserverOfResult, 'xmlResult.php',
                ['f': 'xml', 'test': params.resultId, 'r': params.resultId, 'multistepFormat': '1'], ContentType.TEXT, [Accept: 'application/xml'])
    }

}
