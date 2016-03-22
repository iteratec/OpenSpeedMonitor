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

import de.iteratec.osm.measurement.environment.WebPageTestServer

/**
 * Acts as a proxy of wptservers. So the actions pass all calls from php-wptmonitors to respective php-wptserver.
 * Some calls catch some infos and write them to db.
 * @author nkuhn
 *
 */
class WptProxyController {
	
	ProxyService proxyService
	
	def index() {
		render 'Index'
	}

	/**
	 * Persists all non-existent {@link Location}s the REST-call to /getLocations.php of wptserver (specified by url-param params.wptserver) delivers.
	 * 
	 * Redirects to /getLocations.php of wptserver afterwards.
	 * 
	 * @return
	 */
	def getLocations() {
		def wptserver = WebPageTestServer.findByProxyIdentifier(params.wptserver)

		if (wptserver) {
			if (log.infoEnabled) log.info("redirect to ${wptserver.baseUrl}getLocations.php")
			
			proxyService.fetchLocations(wptserver)

			redirect(url: "${wptserver.baseUrl}getLocations.php", params: params)
		} else {
			if (log.infoEnabled) log.info('Fail on getLocations')

			response.sendError(404)
		}
	}

	/**
	 * Forwards POST-request to /runtest.php of wptserver (specified by url-param params.wptserver) and sends response-stream as response, directly. 
	 * No impact in openSpeedMonitor.
	 * @return
	 */
	def runtest() {
		def wptserver = WebPageTestServer.findByProxyIdentifier(params.wptserver)

		if (wptserver) {
			if (log.infoEnabled) log.info('Test run...')
			if (log.infoEnabled) log.info("params=$params")
			
			response.outputStream << proxyService.runtest(wptserver, params).data
		} else {
			if (log.infoEnabled) log.info('Fail on runtest')

			response.sendError(404)
		}
	}

	/**
	 * Redirects to the file-download-url of wptserver (specified by url-param params.wptserver), directly.
	 * No impact in openSpeedMonitor.
	 * @return
	 */
	def resultFileDownload() {
		def wptserver = WebPageTestServer.findByProxyIdentifier(params.wptserver)

		if (wptserver) {
			log.info("routing to filedownload ${wptserver.baseUrl}results/${params.resultYear}/${params.resultMonth}/${params.resultDay}/${params.resultFolder}/${params.resultId}/${params.fileToDownload}")
			redirect(url: "${wptserver.baseUrl}results/${params.resultYear}/${params.resultMonth}/${params.resultDay}/${params.resultFolder}/${params.resultId}/${params.fileToDownload}")
		} else {
			if (log.infoEnabled) log.info('Fail on resultFileDownload')

			response.sendError(404)
		}
	}

	/**
	 * Redirects to the detailed result-page of wptserver (specified by url-param params.wptserver) for result with test-id params.resultId, directly. 
	 * No impact in openSpeedMonitor.
	 * @return
	 */
	def result() {
		def wptserver = WebPageTestServer.findByProxyIdentifier(params.wptserver)


		if (wptserver) {
			if (log.infoEnabled) log.info("Routing to result ${wptserver.baseUrl}result/${params.resultId}")
			redirect(url: "${wptserver.baseUrl}result/${params.resultId}")
		} else {
			if (log.infoEnabled) log.info('Fail on result')

			response.sendError(404)
		}
	}

	/**
	 * Persists all non-existent {@link EventResult}s the REST-call to /xmlResult.php of wptserver (specified by url-param params.wptserver) delivers.
	 * The following result-dependent domains get persisted, too (if they didn't already exist before): {@link Job}, {@link MeasuredEvent}, 
	 * {@link JobResult}, {@link CsiAggregation}
	 *   
	 * Redirects to /xmlResult.php of wptserver afterwards.
	 * 
	 * @return
	 * @see LocationAndResultPersisterService
	 */
	def xmlResult() {
		def wptserver = WebPageTestServer.findByProxyIdentifier(params.wptserver)

		if (wptserver) {
			log.info "Routing to final xml-result  ${wptserver.baseUrl}xmlResult.php?f=xml&test=${params.resultId}"
			proxyService.fetchResult(wptserver, params)

			redirect(url: "${wptserver.baseUrl}xmlResult.php?f=xml&test=${params.resultId}")
		} else {
			if (log.infoEnabled) log.info('Fail on xmlResult')

			response.sendError(404)
		}
	}
}

