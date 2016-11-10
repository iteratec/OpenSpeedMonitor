package de.iteratec.osm.util
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
        PerformanceLoggingService.IndentationDepth indentation
        Double elapsedMilliSecs
    }

    Set<String> loggedDescriptions = []
    Map executionTimesByDescription = [:].withDefault {new ArrayList<LoggedTime>()}

    /**
     * Add one execution time to this logging session.
     * @param description
     *          Logs Description.
     * @param indentation
     *          Logs indentation depth.
     * @param level
     *          Logs {@link de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth}
     * @param execTime
     *          Time in seconds the logged execution time took.
     */
    void addExecutionTime(String description, PerformanceLoggingService.IndentationDepth indentation, PerformanceLoggingService.LogLevel level, Double execTime) {
        loggedDescriptions.add(description)
        executionTimesByDescription[description].add(
            new LoggedTime(level: level, indentation: indentation, elapsedMilliSecs: execTime)
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
            sb.append("${loggedTimes[0].indentation.prefix} ${description}: ")
            Map countAndTime = loggedTimes.inject([numOfTimes: 0, sumElapsedTime: 0]){Map<Integer, Integer> countAndTime, LoggedTime loggedTime ->
                if (loggedTime.level.getValue() >= level.getValue()){
                    countAndTime["numOfTimes"] += 1
                    countAndTime["sumElapsedTime"] += loggedTime.elapsedMilliSecs
                }
                return countAndTime
            }
            if (loggedTimes.size() > 0){
                sb.append("${countAndTime["numOfTimes"]} call(s) -> took ${countAndTime["sumElapsedTime"].round(3)} seconds (avg ${(countAndTime["sumElapsedTime"]/countAndTime["numOfTimes"]).round(3)})\n")
            }else{
                sb.append("No calls at all.")
            }
        }
        return sb.toString()
    }

}
