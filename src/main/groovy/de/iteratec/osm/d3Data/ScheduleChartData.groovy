package de.iteratec.osm.d3Data

import org.joda.time.DateTime

/**
 * A classification for a schedule chart
 */
class ScheduleChartData {
    String name
    List<ScheduleChartJob> jobs
    // Sorted List
    List<DateTime> allExecutionDates
    List<DateTime> allEndDates

    String discountedJobsLabel
    List<String> discountedJobs

    DateTime startDate
    DateTime endDate
    int agentCount

    ScheduleChartData() {
        name = "Location"
        jobs = new ArrayList<>()

        startDate = new DateTime()
        endDate = new DateTime()

        discountedJobs = new ArrayList<>()
        discountedJobsLabel = "discounted Jobs"

        allExecutionDates = new ArrayList<>()
        allEndDates = new ArrayList<>()
    }

    def addJob(ScheduleChartJob job) {
        jobs.add(job)
        job.executionDates.each {date ->
            allExecutionDates.add(date)
            allEndDates.add(date.plusSeconds(job.durationInSeconds))
        }
        allEndDates.sort()
        allExecutionDates.sort()
    }

    def addDiscountedJob(String jobName) {
        discountedJobs.add(jobName)
    }
}
