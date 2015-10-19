package de.iteratec.osm.d3Data

/**
 * A classification for a schedule chart
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
