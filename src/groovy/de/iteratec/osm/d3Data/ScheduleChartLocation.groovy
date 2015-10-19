package de.iteratec.osm.d3Data

/**
 * Created by mmi on 15.10.2015.
 */
class ScheduleChartLocation {
    String name
    List<ScheduleChartJob> jobs

    ScheduleChartLocation() {
        name = "Location"
        jobs = new ArrayList<>()
    }

    def addJob(ScheduleChartJob job) {
        jobs.add(job)
    }
}
