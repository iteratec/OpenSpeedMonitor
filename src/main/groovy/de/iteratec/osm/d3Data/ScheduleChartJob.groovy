package de.iteratec.osm.d3Data

import org.joda.time.DateTime

/**
 * The smallest unit in a schedule Chart.
 * Apart from its name it consists of a list of execution dates
 * and the duration (in minutes) the job requires on each execution date
 */
class ScheduleChartJob {
    String name
    String description
    List<DateTime> executionDates
    int durationInSeconds
    long linkId

    ScheduleChartJob() {
        name = "Job"
        description = "description"
        executionDates = new ArrayList<>()
        durationInSeconds = 60
        linkId = 0
    }
}
