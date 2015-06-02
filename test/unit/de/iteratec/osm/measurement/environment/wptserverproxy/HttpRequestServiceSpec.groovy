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

import spock.lang.Specification

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(HttpRequestService)
@TestMixin(GrailsUnitTestMixin)
class HttpRequestServiceSpec extends Specification{

    void "add trailing slash if missing"(String url, String expectedResult) {
        when:
        String trailingSlashAdded = service.addTrailingSlashIfMissing(url)

        then:
        assertThat(trailingSlashAdded, is(expectedResult))

        where:
        url << ['http://my-url.com', 'http://my-url.com/details', 'http://my-url.com/']
        expectedResult << ['http://my-url.com/', 'http://my-url.com/details/', 'http://my-url.com/']
    }
    void "remove leading slash if existing"(String urlPart, String expectedResult){
        when:
        String leadingSlashRemoved = service.removeLeadingSlashIfExisting(urlPart)

        then:
        assertThat(leadingSlashRemoved, is(expectedResult))

        where:
        urlPart << ['/my-url-part/', 'my-url-part/', '/my-url-part/with/more/than/one/parts', 'my-url-part/with/more/than/one/parts']
        expectedResult << ['my-url-part/', 'my-url-part/', 'my-url-part/with/more/than/one/parts', 'my-url-part/with/more/than/one/parts']
    }
}
