package de.iteratec.osm.d3Data

import org.joda.time.DateTime

/**
 * Created by mmi on 15.10.2015.
 */
class ScheduleChartJob {
    String name
    List<DateTime> executionDates
    Double durationInMinutes

    ScheduleChartJob() {
        name = "Job"
        executionDates = new ArrayList<>()
        durationInMinutes = 1.0
    }
}
