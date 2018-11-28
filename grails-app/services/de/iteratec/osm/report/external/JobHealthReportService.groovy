package de.iteratec.osm.report.external

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.report.external.provider.GraphiteSocketProvider

class JobHealthReportService {

    GraphiteSocketProvider graphiteSocketProvider

    void reportJobHealthStatusToGraphite(Date date) {
        List<Job> jobsToReport = Job.createCriteria().list {
            eq 'deleted', false
            eq 'active', true
            isNotNull 'jobStatistic'
            jobGroup {
                isNotNull 'jobHealthGraphiteServers'
                isNotEmpty 'jobHealthGraphiteServers'
            }
        }

        if (jobsToReport) {
            log.debug("Starting job health report for the jobs: ${jobsToReport}")
            jobsToReport.each { job ->
                Collection<GraphiteServer> graphiteServers = job.jobGroup.jobHealthGraphiteServers

                graphiteServers.each { graphiteServer ->
                    GraphiteSocket socket = graphiteSocketProvider.getSocket(graphiteServer)

                    String basePath = determineBasePath(job, graphiteServer)

                    if (job.jobStatistic.percentageSuccessfulTestsOfLast5 != null) {
                        socket.sendDate(GraphitePathName.valueOf(basePath + ".percentageSuccessfulTestsOfLast5"), job.jobStatistic.percentageSuccessfulTestsOfLast5, date)
                    }
                    if (job.jobStatistic.percentageSuccessfulTestsOfLast25 != null) {
                        socket.sendDate(GraphitePathName.valueOf(basePath + ".percentageSuccessfulTestsOfLast25"), job.jobStatistic.percentageSuccessfulTestsOfLast25, date)
                    }
                    if (job.jobStatistic.percentageSuccessfulTestsOfLast150 != null) {
                        socket.sendDate(GraphitePathName.valueOf(basePath + ".percentageSuccessfulTestsOfLast150"), job.jobStatistic.percentageSuccessfulTestsOfLast150, date)
                    }
                }
            }
        }
    }

    private static String determineBasePath(Job job, GraphiteServer graphiteServer) {
        List<String> pathElements = []
        pathElements.add(graphiteServer.prefix ?: 'osm')
        pathElements.add('job-health')
        pathElements.add(GraphitePathName.replaceInvalidGraphitePathCharacters(job.jobGroup.name))
        pathElements.add(GraphitePathName.replaceInvalidGraphitePathCharacters(job.script.label))
        pathElements.add(GraphitePathName.replaceInvalidGraphitePathCharacters(job.location.location))
        pathElements.add(GraphitePathName.replaceInvalidGraphitePathCharacters(job.location.browser.name))
        pathElements.add(GraphitePathName.replaceInvalidGraphitePathCharacters(job.id.toString()))
        String basePath = pathElements.join('.')
        return basePath
    }
}