package de.iteratec.osm.d3Data

import org.joda.time.DateTime

/**
 * The smallest unit in a schedule Chart.
 * Apart from its name it consists of a list of execution dates
 * and the duration (in minutes) the job requires on each execution date
 */
class ScheduleChartJob {
    public static final String DEFAULT_NAME = "Job"
    public static final String DEFAULT_DESCRIPTION = "description"
    public static final int DEFAULT_DURATION = 60
    public static final int DEFAULT_LINK_ID = 0
    String name
    String description
    List<DateTime> executionDates
    int durationInSeconds
    long linkId

    ScheduleChartJob() {
        name = DEFAULT_NAME
        description = DEFAULT_DESCRIPTION
        executionDates = new ArrayList<>()
        durationInSeconds = DEFAULT_DURATION
        linkId = DEFAULT_LINK_ID
    }
}
