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
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.PageService

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
	STEP_NOT_RECORDED
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
 * A parser for WPTServer scripts respecting only logData, setEventName,
 * navigate and execAndWait statements.
 * Registered as Spring Bean.
 *  
 * @author dri
 */
class ScriptParser {

	PageService pageService

	// Keywords and pattern used for parsing
	final static String logDataCmd = 'logData'  
	final static String setEventNameCmd = 'setEventName'  
	final static String navigateCmd = 'navigate'  
	final static String execAndWaitCmd = 'execAndWait'  
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
	 * Warnings reporting misplaced setEventName statements
	 */
	public List<ScriptEventNameCmdWarning> warnings
	/**
	 * Pairs of line numbers of detected measured steps
	 * Every even number marks a line where a step begins,
	 * every odd number marks a line where a step ends.
	 */
	public List<Integer> steps

	private PageService pageService
		
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
		warnings << new ScriptEventNameCmdWarning(
			type: ScriptEventNameCmdWarningType.DANGLING_SETEVENTNAME_STATEMENT,
			lineNumber: statements.take(lookBackFrom).reverse().find { it.keyword == setEventNameCmd }.lineNumber
		)
	}
	
	/**
	 * Reports a MISSING_SETEVENTNAME_STATEMENT for the given page view statement.
	 * @param statement
	 */
	private void reportMissingSetEventNameStatement(ScriptStatement statement) {
		assert(statement.isPageViewCmd)
		warnings << new ScriptEventNameCmdWarning(
			type: ScriptEventNameCmdWarningType.MISSING_SETEVENTNAME_STATEMENT,
			lineNumber: statement.lineNumber
		)
	}
	
	private void reportNoStepsFound() {
		warnings << new ScriptEventNameCmdWarning(
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
		warnings = []
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
				if (setEventNameStmtFound && logData)
					reportDanglingSetEventNameCommand(statements, i)
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
				} else {
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
			
			log.info("$stmt : logData $logData, inMV $inMeasuredEvent (${eventNames.size() > 0 ? eventNames.last() : ''}), num $measuredEventsCount")
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

	public List<Page> getTestedPages(){
		List<Page> testedPages = []
		this.eventNames.each {eventName ->
			testedPages << ( MeasuredEvent.findByName(pageService.excludePagenamePart(eventName))?.testedPage ?: Page.findByName(Page.UNDEFINED) )
		}
		return testedPages
	}

	public getMeasuredEvents(){
		List<MeasuredEvent> events = []
		this.eventNames.each {eventName ->
			MeasuredEvent measuredEvent = MeasuredEvent.findByName(pageService.excludePagenamePart(eventName))
			if (measuredEvent) events << measuredEvent
		}
		return events
	}
	
	/**
	 * Initialize parser and interpret the given script.
	 */
	public ScriptParser(String navigationScript) {
		interpret(navigationScript)
	}
	public ScriptParser() {
	}
}