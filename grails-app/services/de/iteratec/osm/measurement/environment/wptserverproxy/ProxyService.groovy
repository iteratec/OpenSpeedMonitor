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

import de.iteratec.osm.util.PerformanceLoggingService
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.*

import java.util.concurrent.locks.ReentrantLock

import de.iteratec.osm.measurement.environment.WebPageTestServer

import static de.iteratec.osm.util.PerformanceLoggingService.LogLevel.DEBUG

interface iListener {
	public String getName()
	public void listenToLocations(GPathResult result, WebPageTestServer wptserver)
	public void listenToResult(
		GPathResult result,
		String har,
		WebPageTestServer wptserver
	)
}

//TODO: Write further tests for this service. Recording of http-responses is necessary!
//				See the following for that:
//				* http://freeside.co/betamax/
//				* https://github.com/robfletcher/betamax/tree/master/examples/grails-betamax 

/**
 * Business logic for functionality of wptserver-proxy. Observers can register with {@link ProxyService#addListener(iListener)}.
 * @author rschuett, nkuhn
 */
class ProxyService {
	
	static transactional = false
	
	protected List<iListener> listener = new ArrayList<iListener>()
	private final ReentrantLock lock = new ReentrantLock()
	
	HttpRequestService httpRequestService
    PerformanceLoggingService performanceLoggingService
	
	/**
	 * Listeners can register as oberservers.
	 * @param listener
	 */
	void addListener(iListener listener) {
		this.listener.add(listener)
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
		
		//In HTTPBuilder version<0.6 arg query doesn't work so we used queryString and encoded query-params ourself
		
//		String urlEncodedQueryString = ""
//		params.each {key, value ->
//			urlEncodedQueryString += URLEncoder.encode("${key}", "UTF-8") + "=" + URLEncoder.encode("${value}", "UTF-8") + "&"
//		}
//		urlEncodedQueryString=urlEncodedQueryString.substring(0, urlEncodedQueryString.size()-1)
//		log.info("queryStringScript=$urlEncodedQueryString")
			
		return httpRequestService.getClient(wptserver).post(
			path: 'runtest.php',
			query: params,
//			queryString: urlEncodedQueryString,
			contentType: ContentType.TEXT,
			headers : [Accept : 'application/xml']
		)
		
	}
	
	HttpResponseDecorator cancelTest(WebPageTestServer wptserver, Map params) {
		return httpRequestService.getClient(wptserver).post(
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
	void fetchLocations(WebPageTestServer wptserver) {
		
		def locationsResponse = httpRequestService.getWptServerHttpGetResponseAsGPathResult(wptserver, 'getLocations.php', [:], ContentType.TEXT, [Accept: 'application/xml'])
		
		log.info("${this.listener.size} iListener(s) listen to the fetching of locations")
		this.listener.each {
			log.info("calling listenToLocations for iListener ${it.getName()}")
			it.listenToLocations(locationsResponse, wptserver)
		}
	}
	
	/**
	 * Gets result from given wptserver via REST-call.
	 * @param wptserverOfResult
	 * 			Instance of PHP-application webpagetest (see http://webpagetest.org) from which xml-result should be get.
	 * @param params 
	 * 			Must contain resultId.
	 * @return
	 */
	int fetchResult(WebPageTestServer wptserverOfResult, Map params) {
		
		if (log.infoEnabled) {
			log.info("Start Saving result ${wptserverOfResult.baseUrl}result/${params.resultId}")
		}
		GPathResult xmlResultResponse = getXmlResult(wptserverOfResult, params)
		Integer statusCode = xmlResultResponse.statusCode.toInteger()
		def state = [0: 'Failure!', 100: 'Test Pending', 101: 'Test Started', 200: 'Test Finished']
		if (log.infoEnabled) {
			log.info("Result-Status: ${statusCode} (${state[statusCode]})")
		}
		final String jobLabel = xmlResultResponse.data.label.toString()
		
		if (log.infoEnabled) {
			def bolIsInteger = xmlResultResponse.data.runs.toString().isInteger()
			def sizeRuns = xmlResultResponse.data.runs.size()
			
			log.info("xmlResultResponse.data.runs.sizeRuns=${sizeRuns}|")
			log.info("xmlResultResponse.data.runs.toString().isInteger()=${bolIsInteger}|")
		}
		
		if (jobLabel.length() > 0 && statusCode >= 200 && xmlResultResponse.data.runs.toString().isInteger()) {

			// nkuhn, 2015-01-22: disabled cause unused at the moment:
//			def har = httpRequestService.getWptServerHttpGetResponse(wptserverOfResult, 'export.php', ['test': params.resultId], ContentType.TEXT, [Accept : 'application/json'])
//			String harDate = har.data.str
//			log.trace("har=${har.data.str}")

			log.debug("${this.listener.size} iListener(s) listen to the fetching of results")

            try {

                performanceLoggingService.logExecutionTime(DEBUG, "Start of listening to a new successful result of job ${jobLabel}: locking interruptibly", PerformanceLoggingService.IndentationDepth.THREE){
                    lock.lockInterruptibly();
                }

                performanceLoggingService.logExecutionTime(DEBUG, "Listening to a new successful result of job ${jobLabel}", PerformanceLoggingService.IndentationDepth.THREE){
                    this.listener.each {
                        it.listenToResult(
                                xmlResultResponse,
                                '',
                                wptserverOfResult
                        )
                    }
                }

            } finally {
                lock.unlock();
            }
			
		}
		
		return statusCode
		
	}
	
	private GPathResult getXmlResult(WebPageTestServer wptserverOfResult, Map params){
		return httpRequestService.getWptServerHttpGetResponseAsGPathResult(wptserverOfResult, 'xmlResult.php',
				['f': 'xml', 'test': params.resultId, 'r': params.resultId], ContentType.TEXT, [Accept: 'application/xml'])
	}
	
}
