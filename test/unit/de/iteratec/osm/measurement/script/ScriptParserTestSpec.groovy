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

package de.iteratec.osm.measurement.script

import static org.junit.Assert.*
import grails.test.mixin.*
import grails.test.mixin.support.*

@TestMixin(GrailsUnitTestMixin)
class ScriptParserTestSpec {

    void testBV1_Komplett_egon_Wpt_Explorer() {
        ScriptParser parser = new ScriptParser()
		List<ScriptStatement> statements = parser.interpret("""
ignoreErrors	1
combineSteps
///step:Abmelden
///logData	0
///navigate	http://www.example.de
///execAndWait	document.getElementById('TconHrefModul_02').click();
///exec	document.getElementById('searchValue').value='shirt';
///execAndWait	document.getElementById('top_search_submit').click();
navigate	http://www.example.de/navigate/to/a/specific/site
execAndWait	document.querySelector('cssSelector').click();
execAndWait	document.getElementById('selectAThing').click();
execAndWait	document.getElementById('selectAThing').click();
execAndWait	document.querySelector('cssSelector').click();
exec	document.getElementById('idOfFormboxMail').value='example@example.de';
exec	document.getElementById('idOfFormboxPassword').value='examplePass';
execAndWait	document.getElementById('idOfLoginButton').click();
execAndWait	document.querySelector('cssSelector').click();
///logData	1
execAndWait	document.getElementById('idOfLogoutButton').click();
		""")
		checkSizes(
			parser: parser,
			measuredEvents: 8,
			steps: [1, 9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 16, 17, 17, 19, 19],
			eventNames: 0,
			warnings: [9, 10, 11, 12, 13, 16, 17, 19]
			)
		
//		parser.warnings.each {
//			assertEquals(ScriptEventNameCmdWarningType.MISSING_SETEVENTNAME_STATEMENT, it.type)
//		}
		// assert that there are only MISSING_SETEVENTNAME_STATEMENT warnings:
		assertEquals([], parser.warnings*.type - ScriptEventNameCmdWarningType.MISSING_SETEVENTNAME_STATEMENT)
    }
	
	void testEmptyScript() {
		ScriptParser parser = new ScriptParser('')
		checkSizes(
			parser: parser,
			measuredEvents: 0,
			steps: [],
			eventNames: 0,
			warnings: []
			)
	}
	
	void testSetEventNameOnlyScript() {
		ScriptParser parser = new ScriptParser('setEventName 456')
		checkSizes(
			parser: parser,
			measuredEvents: 0,
			steps: [],
			eventNames: 1,
			warnings: [0, 0]
			)
		assertEquals(1, parser.warnings.count { it.type == ScriptEventNameCmdWarningType.DANGLING_SETEVENTNAME_STATEMENT })
		assertEquals(1, parser.warnings.count { it.type == ScriptEventNameCmdWarningType.NO_STEPS_FOUND })
	}
	
	void testPageViewCommandOnlyScript() {
		ScriptParser parser = new ScriptParser('navigate http://example.com')
		checkSizes(
			parser: parser,
			measuredEvents: 1,
			steps: [0, 0],
			eventNames: 0,
			warnings: [0]
			)
		assertEquals(ScriptEventNameCmdWarningType.MISSING_SETEVENTNAME_STATEMENT, parser.warnings[0].type)
	}
	
	void testGlobalLogDataZeroScript() {
		ScriptParser parser = new ScriptParser("""
logData 0
setEventName	eventA
exec dsasda
navigate http://testsite.de
setEventName	eventB
exec dsasda
navigate http://testsite.de
setEventName	eventC
exec dsasda
navigate http://testsite.de
			""")
		checkSizes(
			parser: parser,
			measuredEvents: 0,
			steps: [],
			eventNames: 3,
			)
		assertEquals(1, parser.warnings.count { it.type == ScriptEventNameCmdWarningType.NO_STEPS_FOUND })
	}
	
	void testAlternatingLogData01Script() {
		ScriptParser parser = new ScriptParser("""
//schritt 1
logData 0
exec dsasda
navigate http://testsite.de
logData 1
setEventName	eventA
navigate http://testsite.de
//schritt 2
logData 0
exec dsasda
navigate http://testsite.de
setEventName	eventA
navigate http://testsite.de
//schritt 3
logData 0
exec dsasda
navigate http://testsite.de
logData 1
setEventName	eventA
navigate http://testsite.de
            """)
		checkSizes(
			parser: parser,
			measuredEvents: 2,
			steps: [5, 7, 18, 20],
			eventNames: 3,
			warnings: [12]
			)
		assertEquals(1, parser.warnings.count { it.type == ScriptEventNameCmdWarningType.DANGLING_SETEVENTNAME_STATEMENT })
	}
	
	private void checkSizes(Map params) {
		assertEquals('measured event count', params.measuredEvents, params.parser.measuredEventsCount)
		assertEquals('event names count', params.eventNames, params.parser.eventNames?.size() ?: 0)
		assertEquals('step line numbers', params.steps, params.parser.steps)
		if (params.warnings)
			assertEquals('warning line numbers', params.warnings.sort(false), params.parser.warnings*.lineNumber.sort(false).each { it + 1 })
	}
}