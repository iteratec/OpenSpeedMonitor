package de.iteratec.osm.d3Data

import org.joda.time.DateTime

/**
 * A classification for a schedule chart
 */
class ScheduleChartData {
    public static final String DEFAULT_NAME = "Location"
    public static final String DEFAULT_DISCOUNTED_JOBS_LABEL = "discounted Jobs"
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
        name = DEFAULT_NAME
        jobs = new ArrayList<>()

        startDate = new DateTime()
        endDate = new DateTime()

        discountedJobs = new ArrayList<>()
        discountedJobsLabel = DEFAULT_DISCOUNTED_JOBS_LABEL

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
