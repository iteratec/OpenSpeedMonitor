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
    static final int IGNORE_RECENT_MINUTES = 60

    JobResultPersisterService jobResultPersisterService

    def fillMissingJobResults() {
        Date latestCheckDate = getLatestCheck()?.date
        Date now = new DateTime().minusMinutes(IGNORE_RECENT_MINUTES).toDate()
        if(latestCheckDate) {
            int missingResults = fillMissingJobResultsSinceDate( new DateTime(latestCheckDate))
            new MissingJobResultCheck(date: now, missingResults: missingResults).save()
            log.info("Missing JobResults: $missingResults")
        }
        else {
            DateTime dateDaysAgo =  new DateTime().minusDays(FIRST_CHECK_DAYS_AGO)
            int missingResults = fillMissingJobResultsSinceDate( dateDaysAgo )
            new MissingJobResultCheck(date: now, missingResults: missingResults).save()
            log.info("Missing JobResults: $missingResults")
        }
    }

    private int fillMissingJobResultsSinceDate(DateTime since) {
        int missingJobResults = 0
        getPreparedJobList(since).forEach{ job ->
            def existingResults = getJobResultsSince(job.job, job.since)
            missingJobResults += checkForMissingJobResults(existingResults, new CronExpression(job.execution_schedule), job.since, job.job)
        }
        return missingJobResults
    }

    private List<Map> getPreparedJobList(DateTime since) {
        return Job.findAllByDeletedAndActiveAndExecutionScheduleIsNotNull(false, true).collect {
            ['job': it, 'id': it.id, 'execution_schedule': it.executionSchedule, 'since': since]
        }
    }

    private def getJobResultsSince(Job job, DateTime since) {
        return JobResult.findAllByJobAndDateGreaterThanAndJobResultStatusInList(job, since.toDate(), [JobResultStatus.SUCCESS])
    }

    private int checkForMissingJobResults( List<JobResult> existingResults, CronExpression cronSchedule, DateTime since, Job job) {
        DateTime now = new DateTime().minusMinutes(IGNORE_RECENT_MINUTES) // ignore potentially pending/running tests
        List<DateTime> scheduledDates = getScheduledDates(since.toDate(), now.toDate(), cronSchedule)
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
        return dates.find { Math.abs(dateDist(it, execDate)) < SCHEDULE_THRESHOLD_MINUTES * (1000L * 60L) }
    }

    long dateDist(DateTime d1, DateTime d2) {
        return Math.abs((d1 - d2.millis).millis)
    }

    List<DateTime> getScheduledDates(Date since, Date until, CronExpression cronExpression) {
        List<DateTime> scheduledDates = []
        Date next = cronExpression.getTimeAfter(since)
        while(next.getTime() < until.getTime()) {
            scheduledDates.add( new DateTime(next))
            next = cronExpression.getTimeAfter(next)
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
