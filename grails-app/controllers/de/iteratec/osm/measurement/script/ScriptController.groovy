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

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.PageService
import de.iteratec.osm.util.ControllerUtils
import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

class ScriptController {

	PageService pageService

	private String getScriptI18n() {
		return message(code: 'de.iteratec.iss.script', default: 'Skript')
	}

	void redirectIfNotFound(Script script, def id) {
		def flashMessageArgs = [getScriptI18n(), id];
		if (!script) {
			flash.message = message(code: 'default.not.found.message', args: flashMessageArgs)
			redirect(action: "list")
		}
	}
	
	public Map<String, Object> list() {
		List<Script> scripts
		if (params.sort == 'testedPageNames') {
			scripts = Script.list()
			scripts.sort { it.testedPageNames.join(', ') }
			if (params.order == 'desc')
				scripts.reverse(true)
		} else {
			scripts = Script.list(params)
		}
		[scripts: scripts]
	}

	def index() {
		redirect(action: 'list')
	}
	
	def create() {
		[script: new Script(params), measuredEvents: MeasuredEvent.findAllByNameNotLike("% %") as JSON]
	}
	
	def save() {
		Script s = new Script(params)
		if (!s.save(flush: true)) {
			render(view: 'create', model: [script: s])
			return
		}
		
		def flashMessageArgs = [getScriptI18n(), s.label]
		flash.message = message(code: 'default.created.message', args: flashMessageArgs)
		redirect(action: "list", id: s.id)
	}

	def edit() {
		Script script = Script.get(params.id)
		redirectIfNotFound(script, params.id)
		// only MeasuredEvents whose names do not contain spaces
		[script: script, measuredEvents: MeasuredEvent.findAllByNameNotLike("% %") as JSON]
	}
	
	def update() {
		Script s = Script.get(params.id)
		def flashMessageArgs = [getScriptI18n(), s.label]
		redirectIfNotFound(s, params.id)
		
		if (params.version) {
			def version = params.version.toLong()
			if (s.version > version) {
				s.errors.rejectValue("version", "default.optimistic.locking.failure",
						  [getScriptI18n()] as Object[],
						  "Another user has updated this script while you were editing")
				render(view: 'edit', model: [script: s])
				return
			}
		}

		s.properties = params;
		if (!s.save(flush: true)) {
			render(view: 'edit', model: [script: s])
			return
		}

		flash.message = message(code: 'default.updated.message', args: flashMessageArgs)
		redirect(action: 'edit',  id: s.id)
	}
	
	def delete() {
		Script script = Script.get(params.id)
		redirectIfNotFound(script, params.id)
		def flashMessageArgs = [getScriptI18n(), script.label]

		try {
			script.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: flashMessageArgs)
			redirect(action: "list")
		}
		catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: flashMessageArgs)
			redirect(action: "edit", id: script.id)
		}
	}
	
	def parseScript(String navigationScript) {
		ScriptParser parser = new ScriptParser(pageService, navigationScript)
		Map output = [:]
		if (parser.warnings)
			output.warnings = parser.warnings.groupBy { it.lineNumber }
		else
			output.warnings = []
		output.steps = parser.steps
		output.variables = PlaceholdersUtility.getPlaceholdersUsedInScript(navigationScript).unique()
		render output as JSON
	}


	def getParsedScript(long scriptId, long jobId){
		Script script = Script.get(scriptId)
		Job job = Job.get(jobId)
		String content = ""
		if(job && script){
			content = script.getParsedNavigationScript(job)
		}
		ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.OK, content)
	}

}