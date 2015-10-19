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

import de.iteratec.osm.result.PageService
import spock.lang.Specification

import grails.test.mixin.*
import grails.test.mixin.support.*

@TestMixin(GrailsUnitTestMixin)
class ScriptParserTestSpec extends Specification {

    PageService pageService

    void "Script without setEventName commands results in MISSING_SETEVENTNAME_STATEMENT warnings"() {
        when:
        ScriptParser parser = new ScriptParser(pageService, """
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

        then:
        parser.warnings.size() == 8
        parser.warnings.each { warning ->
            warning == ScriptEventNameCmdWarningType.MISSING_SETEVENTNAME_STATEMENT
        }
    }

    void "Empty script is handled correctly"() {
        when:
        ScriptParser parser = new ScriptParser(pageService, '')

        then:
        parser.measuredEventsCount == 0
        parser.eventNames.size() == 0
        parser.steps == []
        parser.warnings == []
    }


    void "Missing PageCommand after setEventName command results in warning"() {
        when:
        ScriptParser parser = new ScriptParser(pageService, 'setEventName 456')

        then:
        parser.warnings.size() == 2
        parser.warnings*.type.contains(ScriptEventNameCmdWarningType.DANGLING_SETEVENTNAME_STATEMENT)
        parser.warnings*.type.contains(ScriptEventNameCmdWarningType.NO_STEPS_FOUND)
    }

    void "PageViewCommandOnlyScript results in warning"() {
        when:
        ScriptParser parser = new ScriptParser(pageService, 'navigate http://example.com')

        then:
        parser.warnings.size() == 1
        parser.warnings[0].type == ScriptEventNameCmdWarningType.MISSING_SETEVENTNAME_STATEMENT
        parser.measuredEventsCount == 1
        parser.steps.size() == 2
        parser.eventNames.size() == 0
    }


    void "GlobalLogDataZeroScript results in warning"() {
        when:
        ScriptParser parser = new ScriptParser(pageService, """
                    void testGlobalLogDataZeroScript() {
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

        then:
        parser.measuredEventsCount == 0
        parser.steps == []
        parser.eventNames.size() == 3
        parser.warnings.size() == 1
        parser.warnings[0].type == ScriptEventNameCmdWarningType.NO_STEPS_FOUND
    }

    void "alternatingLogData01Script results in warning" () {
        when:
		ScriptParser parser = new ScriptParser(pageService, """
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


        then:
        parser.measuredEventsCount == 2
        parser.steps == [5, 7, 18, 20]
        parser.eventNames.size() == 3
        parser.warnings.size() == 1
        parser.warnings[0].type == ScriptEventNameCmdWarningType.DANGLING_SETEVENTNAME_STATEMENT
    }

    void "allPageLoadEvents includes logData0 events" () {
        when:
        ScriptParser parser = new ScriptParser(pageService, """
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

        then:
        parser.allPageLoadEvents == 9
    }
}