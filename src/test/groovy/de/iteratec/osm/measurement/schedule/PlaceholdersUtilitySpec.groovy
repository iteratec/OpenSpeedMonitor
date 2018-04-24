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

import de.iteratec.osm.measurement.script.PlaceholdersUtility
import de.iteratec.osm.measurement.script.Script
import grails.buildtestdata.BuildDomainTest
import grails.buildtestdata.mixin.Build
import spock.lang.Specification

/**
 * Utility class for parsing and validating placeholders used in Scripts and Jobs
 * @see pts.js Contains regex pattern used for syntax highlighting  
 * @author dri
 */
@Build(Script)
class PlaceholdersUtilitySpec extends Specification implements BuildDomainTest<Script> {

    public static final String NAV_SCRIPT_USING_VARS = 'setEventName ${eventName}\nnavigate ${targeturl}'

    void "parametrized navigationScript parsing given all used variables"() {
        given: "a script using multiple variables"
        Map fullScriptVariableMap = [
            targeturl: 'http://example.com',
            eventName: 'HOMEPAGE'
        ]
        Script script = Script.buildWithoutSave(navigationScript: NAV_SCRIPT_USING_VARS)

        when: "this script get parsed with a variable map containing all variables"
        String parsedWithVariables = PlaceholdersUtility.getParsedNavigationScript(script.navigationScript, fullScriptVariableMap)

        then: "all variables in the script got replaced correctly"
        parsedWithVariables == 'setEventName HOMEPAGE\nnavigate http://example.com'
    }

    void "parametrized navigationScript parsing given just some of the used variables"() {
        given: "a script using multiple variables"
        Map incompleteScriptVariableMap = [
                targeturl: 'http://example.com',
        ]
        Script script = Script.buildWithoutSave(navigationScript: NAV_SCRIPT_USING_VARS)

        when: "this script get parsed with a variable map containing just one of two variables"
        String parsedWithVariables = PlaceholdersUtility.getParsedNavigationScript(script.navigationScript, incompleteScriptVariableMap)

        then: "no exception was thrown, the variables contained in variable map got replaced  respective map values, the rest " +
                "got replaced with empty String"
        parsedWithVariables == 'setEventName \nnavigate http://example.com'
    }

    void "parametrized navigationScript parsing given no variables"() {
        given: "a script using multiple variables"
        Map emptyScriptVariableMap = [:]
        Script script = Script.buildWithoutSave(navigationScript: NAV_SCRIPT_USING_VARS)

        when: "this script get parsed with an empty variable map"
        String parsedWithVariables = PlaceholdersUtility.getParsedNavigationScript(script.navigationScript, emptyScriptVariableMap)

        then: "no exception was thrown and all variables got replaced with empty String"
        parsedWithVariables == 'setEventName \nnavigate '
    }


}