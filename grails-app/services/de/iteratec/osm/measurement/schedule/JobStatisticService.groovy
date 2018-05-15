package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.WptStatus
import grails.transaction.Transactional

@Transactional
class JobStatisticService {

    /**
     * A {@link JobStatistic} is created and associated to given job if no one already exists.
     * The statistic is updated.
     * @param job
     */
    public void updateStatsFor(Job job) {

        List<JobResult> results = getLast150CompletedJobResultsFor(job)

        JobStatistic stat = getStatOf(job)
        stat.percentageSuccessfulTestsOfLast150 = results.size() == 150 ?
            (results.count{it.httpStatusCode==WptStatus.Completed.getWptStatusCode()}/150)*100 :
            null
        stat.percentageSuccessfulTestsOfLast25 = results.size() >= 25 ?
            (results.take(25).count{it.httpStatusCode==WptStatus.Completed.getWptStatusCode()}/25)*100 :
            null
        stat.percentageSuccessfulTestsOfLast5 = results.size() >= 5 ?
            (results.take(5).count{it.httpStatusCode==WptStatus.Completed.getWptStatusCode()}/5)*100 :
            null

        stat.save(failOnError: true)
    }

    private JobStatistic getStatOf(Job job){
        JobStatistic jobStatistic = job.jobStatistic
        if (jobStatistic == null){
            jobStatistic = new JobStatistic()
            job.jobStatistic = jobStatistic
            job.save(failOnError: true)
        }
        return jobStatistic
    }

    List<JobResult> getLast150CompletedJobResultsFor(Job job){
        return  JobResult.createCriteria().list{
            eq("job", job)
            order("date", "desc")
            ge("httpStatusCode"	, 200)
            maxResults(150)
        }
    }
}
