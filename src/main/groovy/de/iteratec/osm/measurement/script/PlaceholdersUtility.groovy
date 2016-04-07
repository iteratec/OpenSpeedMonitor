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

import java.util.regex.Matcher

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.script.Script

class PlaceholdersUtility {	
	public static String getParsedNavigationScript(String navigationScript, Map<String, String> variables) {
		if (!navigationScript)
			return null
			
		String template = navigationScript;
		// replace all variables with their values
		variables?.each { k, v -> template = template.replaceAll(/\$\{$k\}/, v) }
		// replace all undefined variables with empty strings
		return template.replaceAll(/\$\{[^}]+\}/, '')
	}
	
	/**
	 * Returns the placeholders defined in the given Job along with their values augmented by a list of 
	 * placeholders used in the given Script but not defined in the given Job.
	 */
	public static def mergeDefinedAndUsedPlaceholders(Job job, Script script) {
		List usedPlaceholders = []
		Map usedPlaceholdersMap = [:]
		List scriptVariables = (script && script.navigationScript) ? getPlaceholdersUsedInScript(script) : []
		scriptVariables.each { usedPlaceholdersMap << [(it): null] }
		Map mergedVariables = job ? usedPlaceholdersMap << job.variables : usedPlaceholdersMap
		// variables in job but not in script? then not editable
		List entries = []
		mergedVariables.each {
			entries << [ name: it.key,
			  value: it.value,
		      editable: (it.key in scriptVariables) ]
		}
		return entries
	}
	
	public static List<String> getPlaceholdersUsedInScript(Script script) {
		getPlaceholdersUsedInScript(script.navigationScript)
	}
	
	public static List<String> getPlaceholdersUsedInScript(String navigationScript) {
		Matcher placeholders = (navigationScript =~ /\$\{(\w+)\}/)
		return placeholders.collect { it[1] }
	}
}