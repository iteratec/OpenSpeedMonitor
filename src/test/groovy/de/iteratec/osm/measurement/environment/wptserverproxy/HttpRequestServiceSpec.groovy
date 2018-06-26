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

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class HttpRequestServiceSpec extends Specification implements ServiceUnitTest<HttpRequestService> {

    void "add trailing slash if missing"(String url, String expectedResult) {
        when: "the services should remove the trailing slash"
        String trailingSlashAdded = service.addTrailingSlashIfMissing(url)

        then: "it is actually removed, if it existed"
        trailingSlashAdded == expectedResult

        where:
        url                         | expectedResult
        'http://my-url.com'         | 'http://my-url.com/'
        'http://my-url.com/details' | 'http://my-url.com/details/'
        'http://my-url.com/'        | 'http://my-url.com/'
    }

    void "remove leading slash if existing"(String urlPart, String expectedResult){
        when: "the services should remove the leading slash"
        String leadingSlashRemoved = service.removeLeadingSlashIfExisting(urlPart)

        then: "it is actually removed, if it existed"
        leadingSlashRemoved == expectedResult

        where:
        urlPart                                 | expectedResult
        '/my-url-part/'                         | 'my-url-part/'
        'my-url-part/'                          | 'my-url-part/'
        '/my-url-part/with/more/than/one/parts' | 'my-url-part/with/more/than/one/parts'
        'my-url-part/with/more/than/one/parts'  | 'my-url-part/with/more/than/one/parts'
    }
}
