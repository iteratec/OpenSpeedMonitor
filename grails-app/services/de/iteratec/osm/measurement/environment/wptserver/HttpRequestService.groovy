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

import de.iteratec.osm.measurement.environment.WebPageTestServer
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.HttpBuilder

import static groovyx.net.http.HttpBuilder.configure

class HttpRequestService {

    GPathResult getWptServerHttpGetResponse(WebPageTestServer wptserver, String path, Map query, String contentType, Map headers) {
        return getRestClientFrom(wptserver).get {
            request.uri.path = addLeadingSlashIfMissing(path)
            request.contentType = contentType
            request.uri.query = query
            headers.each {k,v->request.headers[k] = v}
        }
    }

    HttpBuilder getRestClientFrom(WebPageTestServer wptserver) {
        return getRestClient(wptserver?.baseUrl)
    }

    GPathResult getJsonResponse(String url, String path, queryParams) {
        return getRestClient(url).get{
            request.uri.path = addLeadingSlashIfMissing(path)
            request.uri.query = queryParams
            request.contentType = 'application/json'
            request.headers['Accept'] = 'application/json'
        }
    }

    HttpBuilder getRestClient(String url) {
        if (!url) {
            throw new IllegalArgumentException("Cant' build rest client for url: ${url}")
        }
        return configure {
            request.uri = url
        }
    }

    String addTrailingSlashIfMissing(String url) {
        return url.endsWith('/') ? url : "${url}/"
    }

    String addLeadingSlashIfMissing(String url) {
        return url.startsWith('/') ? url : "/${url}"
    }

    String removeLeadingSlashIfExisting(String urlPart) {
        return urlPart.startsWith('/') ? urlPart.drop(1) : urlPart
    }
    /**
     * Turn a String representation of the query from a URL into a map of parameter
     * which can be used with other Methods from this service
     * @param query
     * @return
     */
    Map splitQueryStringToMap(String query) {
        def map = [:]
        query.split("&").each { keyValue ->
            keyValue.split("=").with { map[it[0]] = it[1] }
        }
        return map
    }

}
