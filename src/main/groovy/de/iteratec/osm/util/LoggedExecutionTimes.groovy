package de.iteratec.osm.util

import static de.iteratec.osm.util.PerformanceLoggingService.INDENTATION_CHAR

/**
 * Assembled execution times.
 * Created by nkuhn on 04.11.16.
 * @see PerformanceLoggingService
 */
class LoggedExecutionTimes {

    /**
     * Single logged execution time.
     */
    class LoggedTime {
        PerformanceLoggingService.LogLevel level
        Integer indentationDepth
        Double elapsedMilliSecs
    }

    Set<String> loggedDescriptions = []
    Map executionTimesByDescription = [:].withDefault {new ArrayList<LoggedTime>()}

    /**
     * Add one execution time to this logging session.
     * @param description
     *          Logs Description.
     * @param indentationDepth
     *          Logs indentationDepth depth.
     * @param level
     *          Logs {@link de.iteratec.osm.util.PerformanceLoggingService.LogLevel}
     * @param execTime
     *          Time in seconds the logged execution time took.
     */
    void addExecutionTime(String description, Integer indentationDepth, PerformanceLoggingService.LogLevel level, Double execTime) {
        loggedDescriptions.add(description)
        executionTimesByDescription[description].add(
            new LoggedTime(level: level, indentationDepth: indentationDepth, elapsedMilliSecs: execTime)
        )
    }
    /**
     * Get a String representation of all the logged execution times assembled within this instance.
     * @param level
     *          The {@link de.iteratec.osm.util.PerformanceLoggingService.LogLevel} of logged execution times to include
     *          in String representation.
     * @return  String representation of all the logged execution times assembled within this instance.
     */
    String getRepresentation(PerformanceLoggingService.LogLevel level){
        StringBuilder sb = new StringBuilder()
        loggedDescriptions.each {description->
            ArrayList<LoggedTime> loggedTimes = executionTimesByDescription[description]
            sb.append("${INDENTATION_CHAR*loggedTimes[0].indentationDepth} ${description}: ")
            Map countAndTime = loggedTimes.inject([numOfTimes: 0, sumElapsedTime: 0]){Map<Integer, Integer> countAndTime, LoggedTime loggedTime ->
                if (loggedTime.level.getValue() >= level.getValue()){
                    countAndTime["numOfTimes"] += 1
                    countAndTime["sumElapsedTime"] += loggedTime.elapsedMilliSecs
                }
                return countAndTime
            }
            if (loggedTimes.size() > 0){
                Integer countOfCalls = Integer.valueOf(countAndTime["numOfTimes"])
                Double avgExecTime = countOfCalls > 0 ?  Double.valueOf(countAndTime["sumElapsedTime"] / countOfCalls) : 0d
                sb.append("${countOfCalls} call(s) -> took ${Double.valueOf(countAndTime["sumElapsedTime"]).round(3)} seconds (avg ${avgExecTime.round(3)})\n")
            }else{
                sb.append("No calls at all.")
            }
        }
        return sb.toString()
    }

}
