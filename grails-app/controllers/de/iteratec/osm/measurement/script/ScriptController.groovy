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
		[script: new Script(params),pages: Page.list(), measuredEvents: MeasuredEvent.list() as JSON]
	}
	
	def save() {
		Script s = new Script(params)
		if (!s.save(flush: true)) {
			render(view: 'create', model: [script: s,pages: Page.list(), measuredEvents: MeasuredEvent.list() as JSON])
			return
		}
		createNewPagesAndMeasuredEvents(s)
		def flashMessageArgs = [getScriptI18n(), s.label]
		flash.message = message(code: 'default.created.message', args: flashMessageArgs)
		redirect(action: "list", id: s.id)
	}

	def edit() {
		Script script = Script.get(params.id)
		redirectIfNotFound(script, params.id)
		// only MeasuredEvents whose names do not contain spaces
		[script: script, pages: Page.list() as JSON, measuredEvents: MeasuredEvent.list() as JSON, archivedScripts:   getListOfArchivedScripts(script)]
	}

	private  getListOfArchivedScripts(Script script) {
		def archiveParams = [:]
		archiveParams.order = "desc"
		archiveParams.sort =  "dateCreated"
		def returnList = []
		ArchivedScript.createCriteria().list(archiveParams) {
			eq("script", script)
			projections {
				property("id","id")
				property("dateCreated","dateCreated")
				property("archiveTag","archiveTag")
			}
		}.each{
			def returnValue = [:]
			returnValue["id"] = it[0]
			returnValue["dateCreated"] = it[1]
			returnValue["archiveTag"] = it[2]
			returnList.add(returnValue)
		}
		return returnList
	}

	def update() {
		Script s = Script.get(params.id)
		def flashMessageArgs = [getScriptI18n(), s.label]
		redirectIfNotFound(s, params.id)
		ArchivedScript archivedScript = createArchiveScript(s)
		if (params.version) {
			def version = params.version.toLong()
			if (s.version > version) {
				s.errors.rejectValue("version", "default.optimistic.locking.failure",
						  [getScriptI18n()] as Object[],
						  "Another user has updated this script while you were editing")
				render(view: 'edit', model: [script: s, pages: Page.list() as JSON, measuredEvents:  getListOfArchivedScripts(s)])
				return
			}
		}

		s.properties = params;
		if (!s.save(flush: true)) {
			render(view: 'edit', model: [script: s, pages: Page.list() as JSON, measuredEvents: MeasuredEvent.list() as JSON])
			return
		}
		createNewPagesAndMeasuredEvents(s)
		archivedScript.save(failOnError:true, flush:true)

		flash.message = message(code: 'default.updated.message', args: flashMessageArgs)
		redirect(action: 'edit',  id: s.id)
	}

	private ArchivedScript createArchiveScript(Script s){
		return new ArchivedScript(archiveTag: s.description,
				description: s.description,
				label: s.label,
				navigationScript: s.navigationScript,
				script: s)

	}

	private void createNewPagesAndMeasuredEvents(Script s) {
		ScriptParser parser = new ScriptParser(pageService, s.navigationScript)
		parser.newPages.each { String name ->
			Page.findOrSaveByName(name)
		}
		parser.newMeasuredEvents.each { String measuredEventName, String pageName ->
			def page = Page.findByName(pageName)
			MeasuredEvent.findOrSaveByNameAndTestedPage(measuredEventName, page)
		}
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
		if (parser.errors)
			output.errors = parser.errors.groupBy {it.lineNumber}
		else
			output.errors = []
		output.newPages = parser.newPages
		output.newMeasuredEvents = parser.newMeasuredEvents.keySet()
		output.steps = parser.steps
		output.variables = PlaceholdersUtility.getPlaceholdersUsedInScript(navigationScript).unique()
		render output as JSON
	}

	def getArchivedNavigationScript(long scriptId){
		def navigationScript = ArchivedScript.findById(scriptId).navigationScript
		ControllerUtils.sendObjectAsJSON(response, [
				navigationScript: navigationScript
		])
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

	def loadArchivedScript(long archivedScriptId ){
		Script s = Script.get(params.id)
		createArchiveScript(s).save(failOnError:true, flush:true)
		ArchivedScript archivedScript  = ArchivedScript.get(archivedScriptId)
		s.label = archivedScript.label
		s.description = archivedScript.description
		s.navigationScript = archivedScript.navigationScript
		s.save(failOnError:true, flush:true)
		redirect(action: "edit", id:s.id)
	}
}