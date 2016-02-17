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

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.PageService
import org.grails.databinding.BindUsing

import org.grails.databinding.BindUsing

/**
 * <p>
 * A script used to perform web page test job.
 * </p>
 */
class Script {

    PageService pageService

    static transients = ['pageService']

    /* Default (injected) attributes of GORM */
    Long	id
    /* Automatic timestamping of GORM */
    Date	dateCreated
    Date	lastUpdated

    String label
    @BindUsing({
        obj, source -> source['description']
    })
    String description
    String navigationScript
    int measuredEventsCount = 0

    Collection<Page> testedPages = []

    static hasMany = [ testedPages: Page ]

    static mapping = {
        sort 'label'
        navigationScript(type: 'text')
    }

    static constraints = {
        label(blank: false, maxSize: 255, unique: true)
        description(widget: 'textarea', maxSize: 255)
        navigationScript(widget: 'textarea')
    }

    public String getParsedNavigationScript(Job job) {
        return getParsedNavigationScript(job.variables);
    }

    public static createDefaultScript(String jobLabel) {
        String scriptLabel = "${jobLabel} Script"
        Script script = Script.findByLabel(scriptLabel)
        if (script)
            return script
        else
            return new Script(
                    label: scriptLabel,
                    description: 'This script is just a placeholder (for jobs without an assigned script)',
                    navigationScript: '// Insert code here'
                    )
    }

    public String getParsedNavigationScript(Map<String, String> variables) {
        return PlaceholdersUtility.getParsedNavigationScript(navigationScript, variables)
    }

    private void parseScript() {
        try{
			ScriptParser parser = new ScriptParser(pageService, navigationScript)
            measuredEventsCount = parser.measuredEventsCount
			testedPages = parser.getTestedPages()
		} catch (Exception e) {
			log.error("An error occurred while parsing of script: ${this}", e)
		}
		
	}
	
	def beforeInsert() { parseScript() }
	def beforeUpdate() { parseScript() }
	
	@Override	 
	public String toString() {
		return "Script ${this.label}";
	}
}