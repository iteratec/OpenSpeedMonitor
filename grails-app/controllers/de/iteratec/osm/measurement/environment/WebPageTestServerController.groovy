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

    static scaffold = true

    def save() {
        WebPageTestServer webPageTestServer = new WebPageTestServer(params)
        if (!webPageTestServer.baseUrl.endsWith('/')) {
            webPageTestServer.baseUrl = webPageTestServer.baseUrl + "/"
        }
        if (!webPageTestServer.save(flush: true)) {
            render(view: 'create', model: [webPageTestServer: webPageTestServer])
            return
        } else {
            redirect(action: "list", id: webPageTestServer.id)
        }
    }


    def update() {
        def webPageTestServerInstance = WebPageTestServer.get(params.id)
        if (!webPageTestServerInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'connectivityProfile.label', default: 'ConnectivityProfile'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (webPageTestServerInstance.version > version) {
                webPageTestServerInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'connectivityProfile.label', default: 'ConnectivityProfile')] as Object[],
                          "Another user has updated this ConnectivityProfile while you were editing")
                render(view: "edit", model: [connectivityProfileInstance: webPageTestServerInstance])
                return
            }
        }

        webPageTestServerInstance.properties = params
        if (!webPageTestServerInstance.baseUrl.endsWith('/')) {
            webPageTestServerInstance.baseUrl = webPageTestServerInstance.baseUrl + "/"
        }

        if (!webPageTestServerInstance.save(flush: true)) {
            render(view: "edit", model: [connectivityProfileInstance: webPageTestServerInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'webPageTestServer.label', default: 'WebPageTestServer'), webPageTestServerInstance.label])
        redirect(action: "list")
    }

	public Map<String, Object> loadLocations() {
		WebPageTestServer webPageTestServer = WebPageTestServer.get(params.id)
		if (webPageTestServer == null){
			flash.message i18nService.msg('', "No WebPageTestServer found with id ${params.id}")
		}else{
			proxyService.fetchLocations(webPageTestServer)
		}
		redirect(action: show, id: params.id)
 	}

}
