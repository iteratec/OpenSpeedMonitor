package de.iteratec.osm.d3Data

import org.joda.time.DateTime

/**
 * A classification for a schedule chart
 */
class ScheduleChartData {
    String name
    List<ScheduleChartJob> jobs

    String discountedJobsLabel
    List<String> discountedJobs

    DateTime startDate
    DateTime endDate

    ScheduleChartData() {
        name = "Location"
        jobs = new ArrayList<>()

        startDate = new DateTime()
        endDate = new DateTime()

        discountedJobs = new ArrayList<>()
        discountedJobsLabel = "discounted Jobs"
    }

    def addJob(ScheduleChartJob job) {
        jobs.add(job)
    }

    def addDiscountedJob(String jobName) {
        discountedJobs.add(jobName)
    }
}
