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

import ch.qos.logback.classic.Logger
import de.iteratec.osm.csi.Page
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.PageService
import org.slf4j.LoggerFactory

/**
 * Represents a one-line statement in a script
 *
 * @author dri
 */
class ScriptStatement {
    /**
     * The keyword of the statement, e.g. logData, exec, ...
     */
    String keyword
    String parameter
    Boolean stepStart = null
    Boolean stepEnd = null
    /**
     * True if this statement is of type navigate or execAndWait
     */
    boolean isPageViewCmd = false
    /**
     * Line this statement was found in the parsed script
     */
    int lineNumber

    public String toString() {
        "$lineNumber: $keyword $parameter ($isPageViewCmd)"
    }
}

/**
 * Possible cases of misplacing setEventName statements.
 * Needed by ScriptEventNameCmdWarning
 *
 * @author dri
 */
enum ScriptEventNameCmdWarningType {
    /**
     * setEventName has no effect because it is not followed by a page view statement.
     */
    DANGLING_SETEVENTNAME_STATEMENT,
    /**
     *  A page view statement is not preceded by a setEventName statement.
     */
    MISSING_SETEVENTNAME_STATEMENT,
    /**
     * The number of measured events is zero in a parsed script.
     */
    NO_STEPS_FOUND,
    /**
     * This is a valid step (setEventName statement followed by a page view command)
     * but logData is set to zero.
     */
    STEP_NOT_RECORDED,

    /**
     * There is no page defined. Therefore we have to assign the measuredEvent to the page "unknown"
     */
    NO_PAGE_DEFINED

}

enum ScriptErrorEnum {
    /**
     * Page and MeasuredEvent already exist but the MeasuredEvent is assigned to a different page
     */
    WRONG_PAGE,

    /**
     * EventName contains more then one ;;;
     */
    TOO_MANY_SEPARATORS,

    /**
     * MeasuredEvent is used twice
     */
    MEASUREDEVENT_NOT_UNIQUE,

    /**
     * Urls have to start with http(s)://
     */
    WRONG_URL_FORMAT,

    /**
     * Pages can not be defined by using variables
     */
    VARIABLE_NOT_SUPPORTED
}

/**
 * A warning reporting misplaced setEventName statements in a script
 *
 * @author dri
 */
class ScriptEventNameCmdWarning {
    ScriptEventNameCmdWarningType type
    /**
     * The line in the script responsible for this warning
     */
    int lineNumber
}

/**
 * An error reporting wrong statements in a script
 *
 */
class ScriptEventNameCmdError {
    ScriptErrorEnum type
    /**
     * The line in the script responsible for this warning
     */
    int lineNumber
}

class CorrectPageForMeasuredEvent {
    String correctPageName
    int lineNumber
}

/**
 * A parser for WPTServer scripts respecting only logData, setEventName,
 * navigate and execAndWait statements.
 * Registered as Spring Bean.
 *
 * @author dri
 */
class ScriptParser {

    PageService pageService

    // Time in seconds a page load command needs
    final int TIME_PER_STEP = 5
    // Delay for a script (Time every script needs) in seconds
    final int DELAY_PER_SCRIPT = 60

    // Keywords and pattern used for parsing
    final static String logDataCmd = 'logData'
    final static String setEventNameCmd = 'setEventName'
    final static String navigateCmd = 'navigate'
    final static String execAndWaitCmd = 'execAndWait'
    static Logger log = LoggerFactory.getLogger(ScriptParser.class)

    /**
     * This pattern matches one line and ignores any leading and trailing whitespace.
     * After any leading whitespace, a string of alphanumeric characters is expected (the keyword)
     * After the keyword, if a parameter is given at least one whitespace character (space or tab)
     * is expected, followed by the parameter.
     *
     * The inner group (the parameter) must be matched non-greedily so it does not
     * match trailing whitespace appearing after the parameter at the end of the line: (.+?)
     *
     * The second outer group uses a special Groovy syntax (?: at the beginning of the group)
     * so it is matched but ignored. This results in the first group yielding the keyword
     * and the second group yielding the parameter.
     */
    final static def parsePattern = ~/^\s*(\w+)(?:[ \t]+(.+?))?\s*$/

    /**
     * All event names (arguments of setEventName statements found in the parsed script)
     */
    public List<String> eventNames
    /**
     * The number of measuredEvents found in the parsed script
     */
    public int measuredEventsCount
    /**
     * The number of navigate and execAndWait command in the parsed script
     * Commands in LogData 0 sections are included
     */
    public int allPageLoadEvents
    /**
     * Warnings reporting misplaced setEventName statements
     */
    public List<ScriptEventNameCmdWarning> warnings
    /**
     * Errors that are severe enough to prevent saving of the script
     */
    public List<ScriptEventNameCmdError> errors
    /**
     * Pairs of line numbers of detected measured steps
     * Every even number marks a line where a step begins,
     * every odd number marks a line where a step ends.
     */
    public List<Integer> steps

    public Set<String> newPages
    public List<Page> testedPages
    public Map<String, String> newMeasuredEvents
    public Set<CorrectPageForMeasuredEvent> correctPageName
    public Set<String> allMeasuredEvents

    private PageService pageService

    Map<String, String> getNewMeasuredEvents() {
        return newMeasuredEvents
    }

    Set<String> getNewPages() {
        return newPages
    }
/**
 * Parse the given navigationScript
 * @param navigationScript Must be not null
 */
    private List<ScriptStatement> parse(String navigationScript) {
        List<ScriptStatement> statements = []

        navigationScript.eachLine { String line, int lineNumber ->
            line.find(parsePattern) { match ->
                ScriptStatement stmt = new ScriptStatement(
                        lineNumber: lineNumber,
                        keyword: match[1],
                        parameter: match.size() == 3 ? match[2] : null
                )
                if (stmt.keyword == navigateCmd || stmt.keyword == execAndWaitCmd) {
                    stmt.isPageViewCmd = true
                }
                statements << stmt
            }
        }

        return statements
    }

    /**
     * Reports a DANGLING_SETEVENTNAME_STATEMENT warning at the line of the last setEventName
     * statement found before the lookBackFromth statement.
     */
    private void reportDanglingSetEventNameCommand(List<ScriptStatement> statements, int lookBackFrom) {
        errors << new ScriptEventNameCmdWarning(
                type: ScriptEventNameCmdWarningType.DANGLING_SETEVENTNAME_STATEMENT,
                lineNumber: statements.take(lookBackFrom + 1).reverse().find {
                    it.keyword == setEventNameCmd
                }.lineNumber
        )
    }

    /**
     * Reports a MISSING_SETEVENTNAME_STATEMENT for the given page view statement.
     * @param statement
     */
    private void reportMissingSetEventNameStatement(ScriptStatement statement) {
        assert (statement.isPageViewCmd)
        errors << new ScriptEventNameCmdWarning(
                type: ScriptEventNameCmdWarningType.MISSING_SETEVENTNAME_STATEMENT,
                lineNumber: statement.lineNumber
        )
    }

    private void reportNoStepsFound() {
        errors << new ScriptEventNameCmdWarning(
                type: ScriptEventNameCmdWarningType.NO_STEPS_FOUND,
                lineNumber: 0
        )
    }

    private void reportStepNotRecorded(ScriptStatement statement) {
        warnings << new ScriptEventNameCmdWarning(
                type: ScriptEventNameCmdWarningType.STEP_NOT_RECORDED,
                lineNumber: statement.lineNumber
        )
    }

    private void recordStepFromTo(int start, int end) {
        steps.push(start)
        steps.push(end)
    }

    /**
     * Parse and interpret the given script.
     * Updates measuredEventCount, eventNames and warnings.
     * @param navigationScript The script that should be parsed and interpreted.
     * @return All valid logData, setEventName, navigate and execAndWait statements found or
     * 	null if navigationScript is null.
     */
    public List<ScriptStatement> interpret(String navigationScript) {
        measuredEventsCount = 0
        eventNames = []
        testedPages = []
        newMeasuredEvents = [:]
        newPages = []
        allMeasuredEvents = []
        correctPageName = []

        warnings = []
        errors = []
        steps = []

        if (!navigationScript)
            return null

        Integer stepBegin = null
        List<ScriptStatement> statements = parse(navigationScript)

        // if there is no logData statement in the script, logData defaults to true.
        // Else it is false until enabled by "logData 1"
        boolean logData = true
        boolean inMeasuredEvent = logData
        boolean setEventNameStmtFound = false

        List possibleUnrecordedSteps = []

        statements.eachWithIndex { ScriptStatement stmt, int i ->
            // logData command
            if (stmt.keyword == logDataCmd && (stmt.parameter == '0' || stmt.parameter == '1')) {
                // convert 1/0 to true/false, respectively
                logData = stmt.parameter.toBoolean()
                // if logData is true, a new measured event/step begins at the current logData statement:
                inMeasuredEvent = logData
                if (logData && !stepBegin)
                    stepBegin = i
                // setEventName command
            } else if (stmt.keyword == setEventNameCmd) {
                // if a setEventName statement has been found before and was not cancelled by a
                // navigate/execAndWait statement following it, issue a warning:
                if (setEventNameStmtFound) {
                    reportDanglingSetEventNameCommand(statements, i)
                }
                if (stmt.parameter) {
                    String pageName = ""
                    String measuredEventName = ""
                    Page page
                    MeasuredEvent measuredEvent
                    if (stmt.parameter.split(":::").length == 2) {
                        pageName = stmt.parameter.split(":::")[0]
                        if (pageName.contains('${')) {
                            errors << new ScriptEventNameCmdError(
                                    type: ScriptErrorEnum.VARIABLE_NOT_SUPPORTED,
                                    lineNumber: statements.take(i + 1).reverse().find {
                                        it.keyword == setEventNameCmd
                                    }.lineNumber)
                        }
                        measuredEventName = stmt.parameter.split(":::")[1]
                        page = Page.findByName(pageName)
                        if(page){
                            testedPages.add(page)
                        }
                    } else if (stmt.parameter.split(":::").length > 2) {
                        errors << new ScriptEventNameCmdError(
                                type: ScriptErrorEnum.TOO_MANY_SEPARATORS,
                                lineNumber: statements.take(i + 1).reverse().find {
                                    it.keyword == setEventNameCmd
                                }.lineNumber)
                    } else {
                        measuredEventName = stmt.parameter
                        testedPages.add(Page.findOrCreateByName(Page.UNDEFINED))
                    }
                    if (allMeasuredEvents.contains(measuredEventName)) {
                        errors << new ScriptEventNameCmdError(
                                type: ScriptErrorEnum.MEASUREDEVENT_NOT_UNIQUE,
                                lineNumber: statements.take(i + 1).reverse().find {
                                    it.keyword == setEventNameCmd
                                }.lineNumber)
                    }
                    allMeasuredEvents.add(measuredEventName)
                    measuredEvent = MeasuredEvent.findByName(measuredEventName)
                    if (pageName && !page) {
                        if (!newPages.contains('${') || !newPages.contains('}')){
                            newPages.add(pageName)
                            Page newPage = new Page()
                            newPage.name = pageName
                            testedPages.add(newPage)
                        }
                    }
                    if (measuredEventName && !measuredEvent) {
                        if (!measuredEventName.contains('${') || !measuredEventName.contains('}'))
                            newMeasuredEvents[measuredEventName] = pageName != "" ? pageName : "undefined"
                    }
                    if (pageName && measuredEvent && measuredEvent.testedPage.name != pageName) {
                        errors << new ScriptEventNameCmdError(
                                type: ScriptErrorEnum.WRONG_PAGE,
                                lineNumber: statements.take(i + 1).reverse().find {
                                    it.keyword == setEventNameCmd
                                }.lineNumber)

                        correctPageName << new CorrectPageForMeasuredEvent(
                                correctPageName: measuredEvent.testedPage.name,
                                lineNumber: statements.take(i + 1).reverse().find {
                                    it.keyword == setEventNameCmd
                                }.lineNumber)
                    }
                }

                setEventNameStmtFound = true
                eventNames << stmt.parameter
                // navigate or execAndWait command
            } else if (stmt.isPageViewCmd) {
                if (!logData) {
                    // if logData is false but a valid step is found (setEventName was found and now
                    // a navigate/execAndWait statement) issue a STEP_NOT_RECORDED warning
                    if (setEventNameStmtFound)
//						reportStepNotRecorded(stmt)
                        possibleUnrecordedSteps << i
                    setEventNameStmtFound = false
                } else {
                    if (stmt.keyword == navigateCmd) {
                        if (stmt.parameter &&
                                !stmt.parameter.startsWith("http://") &&
                                !stmt.parameter.startsWith("https://") &&
                                !stmt.parameter.startsWith('${')) {
                            errors << new ScriptEventNameCmdError(
                                    type: ScriptErrorEnum.WRONG_URL_FORMAT,
                                    lineNumber: statements.take(i + 1).reverse().find {
                                        it.keyword == navigateCmd
                                    }.lineNumber)
                        }
                    }
                    if (possibleUnrecordedSteps.size() > 0) {
                        if (!setEventNameStmtFound)
                            possibleUnrecordedSteps.each { reportStepNotRecorded(statements[it]) }
                        possibleUnrecordedSteps = []
                    }
                    // if logData is true and no setEventName statement has been found preceding the
                    // current navigate/execAndWait statement, issue a MISSING_SETEVENTNAME_STATEMENT
                    // warning
                    if (!setEventNameStmtFound)
                        reportMissingSetEventNameStatement(stmt)
                    if (inMeasuredEvent) {
                        measuredEventsCount++
                        // record the current step which started at statement with index stepBegin
                        // and ends at the current statement and reset stepBegin:
                        if (stepBegin) {
                            recordStepFromTo(stepBegin, i)
                            stepBegin = null
                        } else if (logData) {
                            // beginning of the current step is the statement following the statement
                            // which marked the end of the previous step or the first line, if there
                            // were no previous steps:
                            recordStepFromTo(steps.size() > 0 ? steps.last() + 1 : 0, i)
                        }
                    }
                    inMeasuredEvent = true
                    setEventNameStmtFound = false
                }
            }


        }

        if (setEventNameStmtFound && logData)
            reportDanglingSetEventNameCommand(statements, statements.size())

        if (measuredEventsCount == 0)
            reportNoStepsFound()

        // convert statement number of step start/end to corresponding line numbers:
        for (int i = 0; i < steps.size(); i++)
            steps[i] = statements[steps[i]].lineNumber

        // force eventNames to list even if it only contains one item:
        eventNames = [eventNames].flatten()
        steps = [steps].flatten()
        return statements
    }

    public List<Page> getTestedPages() {
        return testedPages
    }

    public getMeasuredEvents() {
        List<MeasuredEvent> events = []
        this.eventNames.each { eventName ->
            MeasuredEvent measuredEvent = MeasuredEvent.findByName(pageService.excludePagenamePart(eventName))
            if (measuredEvent) events << measuredEvent
        }
        return events
    }

    /**
     * Initialize parser and interpret the given script.
     */
    public ScriptParser(PageService pageService, String navigationScript, String navigationScriptName) {
        log.info("Parsing Script: $navigationScriptName")
        this.pageService = pageService
        def statements = interpret(navigationScript)
        allPageLoadEvents = statements.findAll { (it.keyword == navigateCmd) || (it.keyword == execAndWaitCmd) }.size()
    }

    /**
     * Calculates the duration of a navigation script.
     * It uses the number of steps in the script.
     * It also adds time that every Job needs for init and exit etc.
     * @return the duration in seconds
     */
    int calculateDurationInSeconds() {
        return allPageLoadEvents * TIME_PER_STEP + DELAY_PER_SCRIPT
    }

    /**
     * Collects all measured events of the given script.
     *
     * @param navigationScript that is given.
     * @return all measured events of the navigationScript.
     */
    List<MeasuredEvent> getAllMeasuredEvents(String navigationScript) {
        if (!navigationScript)
            return null

        List names = parse(navigationScript).collect {
            if (it.keyword == setEventNameCmd) {
                def eventSpecification = it.parameter.split(":::")
                eventSpecification.length == 2 ? eventSpecification[1] : eventSpecification[0]
            }
        }

        return MeasuredEvent.findAllByNameInList(names)
    }
}
