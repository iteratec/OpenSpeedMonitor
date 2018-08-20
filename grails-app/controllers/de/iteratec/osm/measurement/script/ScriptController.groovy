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
import de.iteratec.osm.util.DataIntegrityViolationExpectionUtil
import grails.converters.JSON
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

class ScriptController {
    PageService pageService
    ScriptService scriptService

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
        [script: new Script(params), pages: Page.list(), measuredEvents: MeasuredEvent.list() as JSON, archivedScripts: ""]
    }

    def save() {
        Script script = new Script(params)
        if (!script.save(flush: true)) {
            render(view: 'create', model: [script: script, pages: Page.list(), measuredEvents: MeasuredEvent.list() as JSON, archivedScripts: ""])
            return
        }
        scriptService.createNewPagesAndMeasuredEvents(new ScriptParser(pageService, script.navigationScript, script.label))
        def flashMessageArgs = [getScriptI18n(), script.label]
        flash.message = message(code: 'default.created.message', args: flashMessageArgs)
        redirect(action: "list")
    }

    def edit() {
        Script script = Script.get(params.id)
        redirectIfNotFound(script, params.id)
//		 only MeasuredEvents whose names do not contain spaces
        [script: script, pages: Page.list() as JSON, measuredEvents: MeasuredEvent.list() as JSON, archivedScripts: getListOfArchivedScripts(script)]
    }

    private getListOfArchivedScripts(Script script) {
        def archiveParams = [:]
        archiveParams.order = "desc"
        archiveParams.sort = "dateCreated"
        def returnList = []
        ArchivedScript.createCriteria().list(archiveParams) {
            eq("script", script)
            projections {
                property("id", "id")
                property("dateCreated", "dateCreated")
                property("versionDescription", "versionDescription")
            }
        }.each {
            def returnValue = [:]
            returnValue["id"] = it[0]
            returnValue["dateCreated"] = it[1]
            returnValue["versionDescription"] = it[2]
            returnList.add(returnValue)
        }
        return returnList
    }

    def update() {
        Script script = Script.get(params.id)
        def flashMessageArgs = [getScriptI18n(), script.label]
        redirectIfNotFound(script, params.id)
        ArchivedScript archivedScript = createArchiveScript(script)
        if (params.version) {
            def version = params.version.toLong()
            if (script.version > version) {
                script.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [getScriptI18n()] as Object[],
                        "Another user has updated this script while you were editing")
                render(view: 'edit', model: [script: script, pages: Page.list() as JSON, measuredEvents: MeasuredEvent.list() as JSON, archivedScripts: getListOfArchivedScripts(script)])
                return
            }
        }

        script.properties = params;
        Script.withNewTransaction {
            if (!script.save(flush: true)) {
                render(view: 'edit', model: [script: script, pages: Page.list() as JSON, measuredEvents: MeasuredEvent.list() as JSON, archivedScripts: getListOfArchivedScripts(script)])
                return
            }
        }
        scriptService.createNewPagesAndMeasuredEvents(new ScriptParser(pageService, script.navigationScript, script.label))
        archivedScript.save(failOnError: true, flush: true)

        flash.message = message(code: 'default.updated.message', args: flashMessageArgs)
        redirect(action: 'edit', id: script.id)
    }

    private ArchivedScript createArchiveScript(Script script) {
        return new ArchivedScript(versionDescription: script.description,
                description: script.description,
                label: script.label,
                navigationScript: script.navigationScript,
                script: script)

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
            String dependency = DataIntegrityViolationExpectionUtil.getEntityNameForForeignKeyViolation(e)
            if (dependency) {
                flashMessageArgs.add(dependency)
                flash.message = message(code: 'default.not.deleted.foreignKeyConstraint.message', args: flashMessageArgs)
            } else {
                flash.message = message(code: 'default.not.deleted.message', args: flashMessageArgs)
            }
            redirect(action: "edit", id: script.id)
        }
    }

    def parseScript(String navigationScript) {
        ScriptParser parser = new ScriptParser(pageService, navigationScript, "Script from Measurement Setup")
        Map output = [:]
        if (parser.warnings)
            output.warnings = parser.warnings.groupBy { it.lineNumber }
        else
            output.warnings = []
        if (parser.errors)
            output.errors = parser.errors.groupBy { it.lineNumber }
        else
            output.errors = []
        output.newPages = parser.newPages
        output.newMeasuredEvents = parser.newMeasuredEvents.collect { "${it.key} (${it.value})" }
        output.correctPageName = parser.correctPageName.groupBy { it.lineNumber }
        output.steps = parser.steps
        output.variables = PlaceholdersUtility.getPlaceholdersUsedInScript(navigationScript).unique()
        render output as JSON
    }

    /**
     * Collects all measured events for a script.
     *
     * @param scriptId The selected script id.
     * @return All measured events for the script id.
     */
    def getMeasuredEventsForScript(String scriptId) {
        Long id = Long.parseLong(scriptId)

        def output = scriptService.getMeasuredEventsForScript(id)

        render output as JSON
    }

    def getArchivedNavigationScript(long scriptId) {
        def navigationScript = ArchivedScript.findById(scriptId).navigationScript
        ControllerUtils.sendObjectAsJSON(response, [
                navigationScript: navigationScript
        ])
    }

    def getParsedScript(long scriptId, long jobId) {
        Script script = Script.get(scriptId)
        Job job = Job.get(jobId)
        String content = ""
        if (job && script) {
            content = script.getParsedNavigationScript(job)
        }
        ControllerUtils.sendResponseAsStreamWithoutModifying(response, HttpStatus.OK, content)
    }

    def loadArchivedScript(long archivedScriptId) {
        Script script = Script.get(params.id)
        def flashMessageArgs = [getScriptI18n(), script.label]
        createArchiveScript(script).save(failOnError: true, flush: true)
        ArchivedScript archivedScript = ArchivedScript.get(archivedScriptId)
        script.label = archivedScript.label
        script.description = archivedScript.description
        script.navigationScript = archivedScript.navigationScript
        script.save(failOnError: true, flush: true)
        flash.message = message(code: 'script.versionControl.load.success', args: flashMessageArgs)
        redirect(action: "edit", id: script.id)
    }

    def updateVersionDescriptionUrl(long archivedScriptId, String newVersionDescription) {
        def archivedScript = ArchivedScript.get(archivedScriptId)
        archivedScript.versionDescription = newVersionDescription
        archivedScript.save(failOnError: true, flush: true)
        ControllerUtils.sendResponseAsStreamWithoutModifying(response, HttpStatus.OK, newVersionDescription)
    }

    def getScriptsForActiveJobGroups() {
        def activeScripts = Job.createCriteria().list {
            eq('active', true)
            isNotNull('script')
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            createAlias('script', 'script')
            projections {
                distinct(['script.id', 'jobGroup.id'])
                property('jobGroup.id', 'jobGroupId')
                property('script.id', 'id')
                property('script.label', 'label')
                property('script.measuredEventsCount', 'numberOfMeasuredEvents')
            }
        }
        return ControllerUtils.sendObjectAsJSON(response, activeScripts)
    }
}
