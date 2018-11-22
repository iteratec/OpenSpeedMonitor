package de.iteratec.osm.system

import de.iteratec.osm.measurement.environment.wptserver.JobResultPersisterService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.JobResultStatus
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.quartz.CronExpression

@Transactional
class MissingJobResultCheckService {
    static final int FIRST_CHECK_DAYS_AGO = 1
    static final long SCHEDULE_THRESHOLD_MINUTES = 3

    JobResultPersisterService jobResultPersisterService

    def fillMissingJobResults() {
        Date latestCheckDate = getLatestCheck()?.date
        int missingResults = 0
        if(latestCheckDate) {
            missingResults = fillMissingJobResultsSinceDate( new DateTime(latestCheckDate).minusMinutes(SCHEDULE_THRESHOLD_MINUTES.toInteger()))
            new MissingJobResultCheck(date: new Date(), missingResults: missingResults).save()
        }
        else {
            DateTime dateDaysAgo =  new DateTime().minusDays(FIRST_CHECK_DAYS_AGO)
            missingResults = fillMissingJobResultsSinceDate( dateDaysAgo )
            new MissingJobResultCheck(date: new Date(), missingResults: missingResults).save()
        }
        if(missingResults){
            log.info("Fixed Missing JobResults: $missingResults")
        }
    }

    private int fillMissingJobResultsSinceDate(DateTime since) {
        int missingJobResults = 0
        getPreparedJobList(since).forEach{ job ->
            def existingResults = getJobResultsSince(job.job, job.since)
            missingJobResults += checkForMissingJobResults(existingResults, job.since, job.job)
        }
        return missingJobResults
    }

    private List<Map> getPreparedJobList(DateTime since) {
        return Job.findAllByDeletedAndActiveAndExecutionScheduleIsNotNull(false, true).collect {
            ['job': it, 'id': it.id, 'since': since]
        }
    }

    private def getJobResultsSince(Job job, DateTime since) {
        return JobResult.findAllByJobAndDateGreaterThan(job, since.toDate())
    }

    private int checkForMissingJobResults( List<JobResult> existingResults, DateTime since, Job job) {
        DateTime now = new DateTime()
        List<DateTime> scheduledDates = getScheduledDates(since.toDate(), now.toDate(), job)
        existingResults.forEach{ jobResult ->
            if(jobResult.executionDate && (jobResult.executionDate <= now.toDate())) {
                DateTime jobResultDate = findDateForJobResult(scheduledDates, jobResult )
                if(jobResultDate) {
                    scheduledDates.remove(jobResultDate)
                }
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
        List<DateTime> closeDates = dates.findAll { Math.abs(dateDist(it, execDate)) < SCHEDULE_THRESHOLD_MINUTES * (1000L * 60L) }
        if(!closeDates.empty) { // Find closest date
            return (closeDates.collect{ [Math.abs(dateDist(it, execDate)), it] }.sort().first())[1]
        }
        return null
    }

    long dateDist(DateTime d1, DateTime d2) {
        return Math.abs((d1 - d2.millis).millis)
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

    MissingJobResultCheck getLatestCheck() {
        return MissingJobResultCheck.last(sort: 'date')
    }
}
