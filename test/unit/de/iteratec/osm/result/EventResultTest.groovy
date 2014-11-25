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

package de.iteratec.osm.result

import static org.junit.Assert.*

import de.iteratec.osm.csi.Page
import grails.test.mixin.*
import grails.test.mixin.support.*

import org.junit.*



/**
 * Test-suite of {@link de.iteratec.osm.result.EventResult}.
 *
 * @author rhc
 */
@TestMixin(GrailsUnitTestMixin)
class EventResultTest {

	JobResult jobResult
	EventResult eventResult
	MeasuredEvent measuredEvent
	Page testedPage
	
    void setUp() {
		
		//Create some data
		testedPage = new Page()
		testedPage.name = "HP"
		
		measuredEvent = new MeasuredEvent()
		measuredEvent.name = "EXAMPLE_HOMEPAGE"		
		measuredEvent.testedPage = testedPage
		
		eventResult = new EventResult()
		eventResult.numberOfWptRun = 1
		eventResult.cachedView = CachedView.CACHED
		eventResult.measuredEvent = measuredEvent
		
		jobResult = new JobResult();
		jobResult.date = new Date(1383574075000L); // 04.11.2013 - 15:07:55
		jobResult.jobConfigLabel = 'HelloLabel';
		jobResult.testId = "123456789_987654321"
		jobResult.wptServerBaseurl = "http://www.example.com/"
    }
	@Test
    public void testBuildTestDetailsURL() {
        URL out = eventResult.buildTestDetailsURL(jobResult, "#waterfall_view"+testedPage.name+measuredEvent.name)
		
		Map<String, List<String>> queryParams = getQueryParams(out.toString())
		
		assertEquals("123456789_987654321", queryParams.get("test").get(0))
		assertEquals("1", queryParams.get("run").get(0))
		assertEquals("1#waterfall_viewHPEXAMPLE_HOMEPAGE", queryParams.get("cached").get(0))
    }
	
	/**
	 * Gives params from given url {@link String}.
	 * @param url
	 * 		URL as {@link String}
	 * @return
	 */
	private static Map<String, List<String>> getQueryParams(String url) {
		try {
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			String[] urlParts = url.split("\\?");
			if (urlParts.length > 1) {
				String query = urlParts[1];
				for (String param : query.split("&")) {
					String[] pair = param.split("=");
					String key = URLDecoder.decode(pair[0], "UTF-8");
					String value = "";
					if (pair.length > 1) {
						value = URLDecoder.decode(pair[1], "UTF-8");
					}
	
					List<String> values = params.get(key);
					if (values == null) {
						values = new ArrayList<String>();
						params.put(key, values);
					}
					values.add(value);
				}
			}
	
			return params;
		} catch (UnsupportedEncodingException ex) {
			throw new AssertionError(ex);
		}
	}
}
