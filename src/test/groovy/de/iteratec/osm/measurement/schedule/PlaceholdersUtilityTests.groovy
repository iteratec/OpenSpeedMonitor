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

package de.iteratec.osm.measurement.schedule

import static org.junit.Assert.assertEquals
import grails.test.mixin.Mock

import org.junit.*

import de.iteratec.osm.measurement.script.PlaceholdersUtility
import de.iteratec.osm.measurement.script.Script

/**
 * Utility class for parsing and validating placeholders used in Scripts and Jobs
 * @see pts.js Contains regex pattern used for syntax highlighting  
 * @author dri
 */
@Mock([Script])
class PlaceholdersUtilityTests {
	/**
	 * This test checks the script templating mechanism.
	 * It creates a template containing two variables but only supplies data
	 * for the first variable. Expected behavior: The first variable is replaced
	 * with the supplied data, the second variable with an empty string.
	 */
	@Test
	void testGetParsedNavigationScript() {
		String template = 'navigate ${targeturl} ${aksdhasjkdhsajkdh}'
		Map variables = [targeturl: 'http://example.com']
		
		Script script = new Script(
			label: 'Testskript',
			description: 'Beschreibung',
			navigationScript: template,
			validationRequest: 'foo'
		);
		script.save(failOnError: true);
		
		String targeturl = 'http://example.com'
		String evaluatedTemplateOneVar = "navigate ${targeturl} "
		String evaluatedTemplateNoVar = "navigate  "
		assertEquals(evaluatedTemplateOneVar, PlaceholdersUtility.getParsedNavigationScript(script.navigationScript, variables));
		assertEquals(evaluatedTemplateNoVar, PlaceholdersUtility.getParsedNavigationScript(script.navigationScript, null));
	}
}