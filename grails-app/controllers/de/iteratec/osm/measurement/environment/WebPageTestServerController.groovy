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

package de.iteratec.osm.measurement.environment

import de.iteratec.osm.measurement.environment.wptserverproxy.ProxyService
import de.iteratec.osm.util.I18nService

class WebPageTestServerController {

	ProxyService proxyService
	I18nService i18nService

    static scaffold = WebPageTestServer

	public Map<String, Object> loadLocations() {
		WebPageTestServer webPageTestServer = WebPageTestServer.get(params.id)
		if (webPageTestServer == null){
			flash.message = i18nService.msg('', "No WebPageTestServer found with id ${params.id}")
		}else{
			List<Location> addedLocations = proxyService.fetchLocations(webPageTestServer)
			String message = "Location-Request was successful."
			if(addedLocations.empty) {
				message += " But no locations were added"
			} else {
				message += " And some locations were added: <br>"
				addedLocations.each {
					message += it.toString() + " <br>"
				}
			}
			flash.message = message

		}
		redirect(action: "show", id: params.id)
 	}

}
