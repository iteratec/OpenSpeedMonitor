package de.iteratec.osm.system

import de.iteratec.osm.measurement.environment.wptserver.JobResultPersisterService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.JobResult
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.quartz.CronExpression

@Transactional
class MissingJobResultCheckService {
    static final int FIRST_CHECK_DAYS_AGO = 1
    static final int SCHEDULE_THRESHOLD_MINUTES = 3

    JobResultPersisterService jobResultPersisterService

    def fillMissingJobResults() {
        Date latestCheckDate = getLatestCheck()?.date
        DateTime startDate = latestCheckDate ? new DateTime(latestCheckDate).minusMinutes(SCHEDULE_THRESHOLD_MINUTES) : new DateTime().minusDays(FIRST_CHECK_DAYS_AGO)
        DateTime now = new DateTime().minusMinutes(SCHEDULE_THRESHOLD_MINUTES)
        int missingResults = fillMissingJobResultsSinceDate( startDate, now)
        persistMissingJobResultCheck(missingResults, now)
        log.info("Fixed Missing JobResults: $missingResults")
    }

    private int fillMissingJobResultsSinceDate(DateTime since, DateTime until) {
        log.debug("Look for Scheduled Dates between $since and $until for")
        int missingJobResults = 0
        getPreparedJobList().each{ job ->
            List<JobResult> existingResults = getJobResultsBetween(job, since, until)
            missingJobResults += checkForMissingJobResults(existingResults, since, until, job)
        }
        return missingJobResults
    }

    private List<Job> getPreparedJobList() {
        return Job.findAllByDeletedAndActiveAndExecutionScheduleIsNotNull(false, true)
    }

    private List<JobResult> getJobResultsBetween(Job job, DateTime since, DateTime until) {
        return JobResult.findAllByJobAndExecutionDateBetween(job, since.toDate(), until.toDate())
    }

    private int checkForMissingJobResults( List<JobResult> existingResults, DateTime since, DateTime until, Job job) {
        List<DateTime> scheduledDates = getScheduledDates(since.toDate(), until.toDate(), job)
        log.debug("Found ${scheduledDates.size()} scheduled dates and ${existingResults.size()} existing jobResults for Job $job")
        existingResults.forEach{ jobResult ->
            DateTime jobResultDate = findDateForJobResult(scheduledDates, jobResult )
            if(jobResultDate) {
                scheduledDates.remove(jobResultDate)
            }
        }
        if(!scheduledDates.empty) {
            log.info("Persist ${scheduledDates.size()} Missing JobResults for: $job")
            persistMissingJobResults(scheduledDates, job)
        }
        return scheduledDates.size()
    }

    DateTime findDateForJobResult(List<DateTime> dates, JobResult jobResult) {
        DateTime execDate = new DateTime(jobResult.executionDate)
        List<DateTime> closeDates = dates.findAll { dateDist(it, execDate) < SCHEDULE_THRESHOLD_MINUTES * (1000L * 60L) }
        if(!closeDates.empty) { // Find closest date
            return closeDates.sort{ dateDist(it, execDate) }.first()
        }
        return null
    }

    long dateDist(DateTime d1, DateTime d2) {
        return Math.abs(d1.millis - d2.millis)
    }

    List<DateTime> getScheduledDates(Date since, Date until, Job job) {
        CronExpression cron = new CronExpression(job.executionSchedule)
        List<DateTime> scheduledDates = []
        Date next = cron.getTimeAfter( job.lastChange > since ? job.lastChange : since )
        while(next.getTime() < until.getTime()) {
            scheduledDates.add( new DateTime(next))
            next = cron.getTimeAfter(next)
        }
        return scheduledDates
    }

    private void persistMissingJobResults(List<DateTime> missingResults, Job job) {
        for(jobResult in missingResults){
            jobResultPersisterService.persistMissingJobResult(job, jobResult.toDate())
        }
    }

    private void persistMissingJobResultCheck(int missingResults, DateTime timestamp) {
        MissingJobResultCheck latest = getLatestCheck()
        if(latest) {
            latest.date = timestamp.toDate()
            latest.missingResults = missingResults
            latest.save()
        }
        else {
            new MissingJobResultCheck(date: timestamp.toDate(), missingResults: missingResults).save()
        }
    }

    MissingJobResultCheck getLatestCheck() {
        return MissingJobResultCheck.last(sort: 'date')
    }
}
