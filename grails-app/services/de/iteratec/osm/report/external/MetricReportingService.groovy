/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

package de.iteratec.osm.report.external

import de.iteratec.osm.ConfigService
import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.BatchActivityUpdater
import de.iteratec.osm.csi.EventCsiAggregationService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.PageCsiAggregationService
import de.iteratec.osm.csi.JobGroupCsiAggregationService
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.dao.JobGroupDaoService
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUtilService
import de.iteratec.osm.report.external.provider.GraphiteSocketProvider
import de.iteratec.osm.result.*
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

/**
 * Reports osm-metrics to external tools.
 */
@Transactional
class MetricReportingService {

    private static final List<String> INVALID_GRAPHITE_PATH_CHARACTERS = ['.', ' ']
    private static final String REPLACEMENT_FOR_INVALID_GRAPHITE_PATH_CHARACTERS = '_'

    GraphiteSocketProvider graphiteSocketProvider
    EventCsiAggregationService eventCsiAggregationService
    JobGroupDaoService jobGroupDaoService
    PageCsiAggregationService pageCsiAggregationService
    JobGroupCsiAggregationService jobGroupCsiAggregationService
    ConfigService configService
    InMemoryConfigService inMemoryConfigService
    BatchActivityService batchActivityService
    CsiAggregationUtilService csiAggregationUtilService

    /**
     * Reports each measurand of incoming result for that a {@link GraphitePathRawData} is configured.
     * @param result
     * 				This EventResult defines measurands to sent and must not be null.
     * @throws NullPointerException
     *             if {@code pathElements} is <code>null</code>.
     * @throws IllegalArgumentException
     *             if at least one of the {@code pathElements} is
     * {@linkplain String#isEmpty() empty} or contains at least one
     *             dot.
     */
    @NotTransactional
    public void reportEventResultToGraphite(EventResult result) {

        Contract.requiresArgumentNotNull("result", result)

        log.info("reporting Eventresult");

        JobGroup jobGroup = result.jobGroup
        Collection<GraphiteServer> servers = jobGroup.graphiteServers.findAll { it.reportEventResultsToGraphiteServer }
        if (servers.size() < 1) {
            return
        }

        MeasuredEvent event = result.measuredEvent
        Page page = result.page
        Browser browser = result.browser
        Location location = result.location

        servers.each { GraphiteServer graphiteServer ->
            log.debug("Sending results to the following GraphiteServer: ${graphiteServer.getServerAdress()}: ")

            GraphiteSocket socket
            try {
                log.info("now the graphiteSocket should be retrieved ...")
                socket = graphiteSocketProvider.getSocket(graphiteServer)
            } catch (Exception e) {
                log.error("GraphiteServer ${graphiteServer} couldn't be reached. The following result couldn't be be sent: ${result}")
                return
            }


            graphiteServer.graphitePathsRawData.findAll { it.cachedView == result.cachedView }.each {
                GraphitePathRawData eachPath ->
                    Double value = SelectedMeasurandType.MEASURAND.getValue(result, eachPath.measurand.toString())
                    if (value != null) {
                        String measurandName = result.cachedView.getGraphiteLabelPrefix()+eachPath.measurand.getGrapthiteLabelSuffix()

                        List<String> pathElements = []
                        pathElements.addAll(eachPath.getPrefix().tokenize('.'))
                        pathElements.add(replaceInvalidGraphitePathCharacters(jobGroup.name))
                        pathElements.add('raw')
                        pathElements.add(replaceInvalidGraphitePathCharacters(page.name))
                        pathElements.add(replaceInvalidGraphitePathCharacters(event.name))
                        pathElements.add(replaceInvalidGraphitePathCharacters(browser.name))
                        pathElements.add(replaceInvalidGraphitePathCharacters(location.uniqueIdentifierForServer == null ? location.location.toString() : location.uniqueIdentifierForServer.toString()))
                        pathElements.add(replaceInvalidGraphitePathCharacters(measurandName))

                        GraphitePathName finalPathName
                        try {
                            finalPathName = GraphitePathName.valueOf(pathElements.toArray(new String[pathElements.size()]));
                        } catch (IllegalArgumentException iae) {
                            log.error("Couldn't write result to graphite due to invalid path: ${pathElements}", iae)
                            return
                        } catch (NullPointerException npe) {
                            log.error("Couldn't write result to graphite due to invalid path: ${pathElements}", npe)
                            return
                        }
                        try {
                            socket.sendDate(finalPathName, value, result.getJobResultDate())
                        } catch (NullPointerException npe) {
                            log.error("Couldn't write result to graphite due to invalid path: ${pathElements}", npe)
                        } catch (GraphiteComunicationFailureException e) {
                            log.error(e)
                        }

                        log.debug("Sent date to graphite: path=${finalPathName}, value=${value} time=${result.getJobResultDate().getTime()}")
                    }
            }

        }
    }

    /**
     * <p>
     * Reports the Event CSI values of the last full hour before(!) the given
     * reporting time-stamp to an external metric tool.
     * </p>
     *
     * @param reportingTimeStamp
     *         The time-stamp for that the last full interval before
     *         should be reported, not <code>null</code>.
     * @since IT-199
     */
    public void reportEventCSIValuesOfLastHour(DateTime reportingTimeStamp, boolean createBatchActivity = true) {
        if (!inMemoryConfigService.areMeasurementsGenerallyEnabled()) {
            log.info("No event csi values are reported cause measurements are generally disabled.")
            return
        }
        BatchActivityUpdater activity = batchActivityService.getActiveBatchActivity(this.class, Activity.UPDATE, "Report last hour CSI Values: ${reportingTimeStamp}", 1, createBatchActivity)

        Contract.requiresArgumentNotNull("reportingTimeStamp", reportingTimeStamp)

        log.debug('reporting csi-values of last hour')
        activity.beginNewStage("Collecting JobGroups", 1)
        Collection<JobGroup> csiGroupsWithGraphiteServers = jobGroupDaoService.findCSIGroups().findAll {
            it.graphiteServers.size() > 0 && it.graphiteServers.any { server -> server.reportCsiAggregationsToGraphiteServer }
        }
        activity.addProgressToStage()
        log.debug("csi-groups to report: ${csiGroupsWithGraphiteServers}")
        activity.beginNewStage("Reporting", csiGroupsWithGraphiteServers.size())
        csiGroupsWithGraphiteServers.eachWithIndex { JobGroup eachJobGroup, int index ->
            activity.addProgressToStage()
            MvQueryParams queryParams = new MvQueryParams()
            ConnectivityProfile.findAll().each { queryParams.connectivityProfileIds.add(it.id) }
            queryParams.jobGroupIds.add(eachJobGroup.getId())
            Date startOfLastClosedInterval = csiAggregationUtilService.resetToStartOfActualInterval(
                    csiAggregationUtilService.subtractOneInterval(reportingTimeStamp, CsiAggregationInterval.HOURLY),
                    CsiAggregationInterval.HOURLY).toDate();
            List<CsiAggregation> mvs = eventCsiAggregationService.getHourlyCsiAggregations(startOfLastClosedInterval, startOfLastClosedInterval, queryParams).findAll { CsiAggregation hmv ->
                hmv.csByWptDocCompleteInPercent != null && hmv.countUnderlyingEventResultsByWptDocComplete() > 0
            }

            log.debug("CsiAggregations to report for last hour: ${mvs}")
            reportAllCsiAggregationsFor(eachJobGroup, AggregationType.MEASURED_EVENT, mvs)
        }
        activity.done()
    }

    /**
     * <p>
     * Reports the Page CSI values of the last Day before(!) the given
     * reporting time-stamp to an external metric tool.
     * </p>
     *
     * @param reportingTimeStamp
     *         The time-stamp for that the last full interval before
     *         should be reported, not <code>null</code>.
     * @since IT-201
     */
    public void reportPageCSIValuesOfLastDay(DateTime reportingTimeStamp, boolean createBatchActivity = true) {

        if (!inMemoryConfigService.areMeasurementsGenerallyEnabled()) {
            log.info("No page csi values of last day are reported cause measurements are generally disabled.")
            return
        }

        if (log.infoEnabled) log.info("Start reporting PageCSIValuesOfLastDay for timestamp: ${reportingTimeStamp}");
        Contract.requiresArgumentNotNull("reportingTimeStamp", reportingTimeStamp)

        BatchActivityUpdater activity = batchActivityService.getActiveBatchActivity(this.class, Activity.UPDATE, "Report last day page CSI Values: ${reportingTimeStamp}", 1, createBatchActivity)
        reportPageCSIValues(CsiAggregationInterval.DAILY, reportingTimeStamp, activity)
        activity.done()


    }

    /**
     * <p>
     * Reports the Page CSI values of the last week before(!) the given
     * reporting time-stamp to an external metric tool.
     * </p>
     *
     * @param reportingTimeStamp
     *         The time-stamp for that the last full interval before
     *         should be reported, not <code>null</code>.
     * @since IT-205
     */
    public void reportPageCSIValuesOfLastWeek(DateTime reportingTimeStamp, boolean createBatchActivity = true) {

        if (!inMemoryConfigService.areMeasurementsGenerallyEnabled()) {
            log.info("No page csi values of last week are reported cause measurements are generally disabled.")
            return
        }

        Contract.requiresArgumentNotNull("reportingTimeStamp", reportingTimeStamp)

        BatchActivityUpdater activity = batchActivityService.getActiveBatchActivity(this.class, Activity.UPDATE, "Report last week page CSI Values: ${reportingTimeStamp}", 1, createBatchActivity)
        reportPageCSIValues(CsiAggregationInterval.WEEKLY, reportingTimeStamp, activity)
        activity.done()
    }

    private void reportPageCSIValues(Integer intervalInMinutes, DateTime reportingTimeStamp, BatchActivityUpdater activity) {
        log.debug("reporting page csi-values with intervalInMinutes ${intervalInMinutes} for reportingTimestamp: ${reportingTimeStamp}")

        def groups = jobGroupDaoService.findCSIGroups().findAll {
            it.graphiteServers.size() > 0 && it.graphiteServers.any { server -> server.reportCsiAggregationsToGraphiteServer }
        }
        int size = groups.size()
        activity.beginNewStage("Report page CSI Values", size)
        groups.eachWithIndex { JobGroup eachJobGroup, int index ->
            activity.addProgressToStage()
            Date startOfLastClosedInterval = csiAggregationUtilService.resetToStartOfActualInterval(
                    csiAggregationUtilService.subtractOneInterval(reportingTimeStamp, intervalInMinutes),
                    intervalInMinutes)
                    .toDate();

            log.debug("getting page csi-values to report to graphite: startOfLastClosedInterval=${startOfLastClosedInterval}")
            CsiAggregationInterval interval = CsiAggregationInterval.findByIntervalInMinutes(intervalInMinutes)
            List<CsiAggregation> pmvsWithData = pageCsiAggregationService.getOrCalculatePageCsiAggregations(startOfLastClosedInterval, startOfLastClosedInterval, interval, [eachJobGroup]).findAll { CsiAggregation pmv ->
                pmv.csByWptDocCompleteInPercent != null && pmv.countUnderlyingEventResultsByWptDocComplete() > 0
            }

            log.debug("reporting ${pmvsWithData.size()} page csi-values with intervalInMinutes ${intervalInMinutes} for JobGroup: ${eachJobGroup}");
            reportAllCsiAggregationsFor(eachJobGroup, AggregationType.PAGE, pmvsWithData)
        }
    }

    /**
     * <p>
     * Reports the Shop CSI values of the last Day before(!) the given
     * reporting time-stamp to an external metric tool.
     * </p>
     *
     * @param reportingTimeStamp
     *         The time-stamp for that the last full interval before
     *         should be reported, not <code>null</code>.
     * @since IT-203
     */
    public void reportShopCSIValuesOfLastDay(DateTime reportingTimeStamp, boolean createBatchActivity = true) {

        if (!inMemoryConfigService.areMeasurementsGenerallyEnabled()) {
            log.info("No shop csi values of last day are reported cause measurements are generally disabled.")
            return
        }

        Contract.requiresArgumentNotNull("reportingTimeStamp", reportingTimeStamp)
        BatchActivityUpdater activity = batchActivityService.getActiveBatchActivity(this.class, Activity.UPDATE, "Report last day shop CSI Values: ${reportingTimeStamp}", 1, createBatchActivity)
        reportShopCSICsiAggregations(CsiAggregationInterval.DAILY, reportingTimeStamp, activity)
        activity.done()
    }

    /**
     * <p>
     * Reports the Shop CSI values of the last week before(!) the given
     * reporting time-stamp to an external metric tool.
     * </p>
     *
     * @param reportingTimeStamp
     *         The time-stamp for that the last full interval before
     *         should be reported, not <code>null</code>.
     * @since IT-205
     */
    public void reportShopCSIValuesOfLastWeek(DateTime reportingTimeStamp, boolean createBatchActivity = true) {

        if (!inMemoryConfigService.areMeasurementsGenerallyEnabled()) {
            log.info("No shop csi values of last week are reported cause measurements are generally disabled.")
            return
        }

        Contract.requiresArgumentNotNull("reportingTimeStamp", reportingTimeStamp)

        BatchActivityUpdater activity = batchActivityService.getActiveBatchActivity(this.class, Activity.UPDATE, "Report last week shop CSI Values: ${reportingTimeStamp}", 1, createBatchActivity)
        reportShopCSICsiAggregations(CsiAggregationInterval.WEEKLY, reportingTimeStamp, activity)
        activity.done()
    }

    private void reportShopCSICsiAggregations(Integer intervalInMinutes, DateTime reportingTimeStamp, BatchActivityUpdater activity) {
        log.debug("reporting shop csi-values with intervalInMinutes ${intervalInMinutes} for reportingTimestamp: ${reportingTimeStamp}")
        def groups = jobGroupDaoService.findCSIGroups().findAll {
            it.graphiteServers.size() > 0 && it.graphiteServers.any { server -> server.reportCsiAggregationsToGraphiteServer }
        }
        int size = groups.size()
        activity.beginNewStage("Report CSI-Values", size)
        groups.eachWithIndex { JobGroup currentJobGroup, int index ->
            activity.addProgressToStage()
            Date startOfLastClosedInterval = csiAggregationUtilService.resetToStartOfActualInterval(
                    csiAggregationUtilService.subtractOneInterval(reportingTimeStamp, intervalInMinutes),
                    intervalInMinutes)
                    .toDate();

            log.debug("getting shop csi-values to report to graphite: startOfLastClosedInterval=${startOfLastClosedInterval}")
            CsiAggregationInterval interval = CsiAggregationInterval.findByIntervalInMinutes(intervalInMinutes)
            List<CsiAggregation> smvsWithData = jobGroupCsiAggregationService.getOrCalculateShopCsiAggregations(startOfLastClosedInterval, startOfLastClosedInterval, interval, [currentJobGroup]).findAll { CsiAggregation smv ->
                smv.csByWptDocCompleteInPercent != null && smv.countUnderlyingEventResultsByWptDocComplete() > 0
            }

            reportAllCsiAggregationsFor(currentJobGroup, AggregationType.JOB_GROUP, smvsWithData)
        }
    }

    private void reportAllCsiAggregationsFor(JobGroup jobGroup, AggregationType aggregationType, List<CsiAggregation> mvs) {
        List<GraphiteServer> graphiteServerToReportTo = jobGroup.graphiteServers.findAll {
            it.reportCsiAggregationsToGraphiteServer
        }
        graphiteServerToReportTo.each { currentGraphiteServer ->
            currentGraphiteServer.graphitePathsCsiData.findAll {
                it.aggregationType == aggregationType
            }.each { GraphitePathCsiData measuredEventGraphitePath ->

                GraphiteSocket socket
                try {
                    socket = graphiteSocketProvider.getSocket(currentGraphiteServer)
                } catch (Exception e) {
                    //TODO: java.net.UnknownHostException can't be catched explicitly! Maybe groovy wraps the exception? But the stacktrace says java.net.UnknownHostException  ...
                    log.error("GraphiteServer ${currentGraphiteServer} couldn't be reached. ${mvs.size()} CsiAggregations couldn't be sent.")
                    return
                }

                log.debug("${mvs.size()} CsiAggregations should be sent to:\nJobGroup=${jobGroup}\nGraphiteServer=${currentGraphiteServer.getServerAdress()}\nGraphitePath=${measuredEventGraphitePath}")
                mvs.each { CsiAggregation mv ->
                    log.debug("Sending ${mv.interval.name} ${aggregationType}-csi-value for:\nJobGroup=${jobGroup}\nGraphiteServer=${currentGraphiteServer.getServerAdress()}\nGraphitePath=${measuredEventGraphitePath}")
                    reportCsiAggregation(measuredEventGraphitePath.getPrefix(), jobGroup, mv, socket)
                }
            }
        }
    }


    private void reportCsiAggregation(String prefix, JobGroup jobGroup, CsiAggregation mv, GraphiteSocket socket) {
        if (mv.interval.intervalInMinutes == CsiAggregationInterval.HOURLY && mv.aggregationType == AggregationType.MEASURED_EVENT) {
            reportHourlyCsiAggregation(prefix, jobGroup, mv, socket)
        } else if (mv.interval.intervalInMinutes == CsiAggregationInterval.DAILY) {
            if (mv.aggregationType == AggregationType.PAGE) {
                reportDailyPageCsiAggregation(prefix, jobGroup, mv, socket)
            } else if (mv.aggregationType == AggregationType.JOB_GROUP) {
                reportDailyShopCsiAggregation(prefix, jobGroup, mv, socket)
            }
        } else if (mv.interval.intervalInMinutes == CsiAggregationInterval.WEEKLY) {
            if (mv.aggregationType == AggregationType.PAGE) {
                reportWeeklyPageCsiAggregation(prefix, jobGroup, mv, socket)
            } else if (mv.aggregationType == AggregationType.JOB_GROUP) {
                reportWeeklyShopCsiAggregation(prefix, jobGroup, mv, socket)
            }
        }
    }

    private void reportHourlyCsiAggregation(String prefix, JobGroup jobGroup, CsiAggregation mv, GraphiteSocket socket) {
        Page page = mv.page
        MeasuredEvent event = mv.measuredEvent
        Browser browser = mv.browser
        Location location = mv.location

        List<String> pathElements = []
        pathElements.addAll(prefix.tokenize('.'))
        pathElements.add(replaceInvalidGraphitePathCharacters(jobGroup.name))
        pathElements.add('hourly')
        pathElements.add(replaceInvalidGraphitePathCharacters(page.name))
        pathElements.add(replaceInvalidGraphitePathCharacters(event.name))
        pathElements.add(replaceInvalidGraphitePathCharacters(browser.name))
        pathElements.add(replaceInvalidGraphitePathCharacters(location.uniqueIdentifierForServer == null ? location.location.toString() : location.uniqueIdentifierForServer.toString()))
        pathElements.add('csi')

        GraphitePathName finalPathName = GraphitePathName.valueOf(pathElements.toArray(new String[pathElements.size()]));
        double valueAsPercentage = mv.csByWptDocCompleteInPercent * 100
        log.debug("Sending ${mv.started}|${valueAsPercentage} as hourly CsiAggregation to graphite-path ${finalPathName}")
        socket.sendDate(finalPathName, valueAsPercentage, mv.started)
    }

    private void reportDailyPageCsiAggregation(String prefix, JobGroup jobGroup, CsiAggregation mv, GraphiteSocket socket) {
        Page page = mv.page

        List<String> pathElements = []
        pathElements.addAll(prefix.tokenize('.'))
        pathElements.add(replaceInvalidGraphitePathCharacters(jobGroup.name))
        pathElements.add('daily')
        pathElements.add(replaceInvalidGraphitePathCharacters(page.name))
        pathElements.add('csi')

        GraphitePathName finalPathName = GraphitePathName.valueOf(pathElements.toArray(new String[pathElements.size()]));
        double valueAsPercentage = mv.csByWptDocCompleteInPercent * 100
        log.debug("Sending ${mv.started}|${valueAsPercentage} as daily page-CsiAggregation to graphite-path ${finalPathName}")
        socket.sendDate(finalPathName, valueAsPercentage, mv.started)
    }

    private void reportDailyShopCsiAggregation(String prefix, JobGroup jobGroup, CsiAggregation mv, GraphiteSocket socket) {
        List<String> pathElements = []
        pathElements.addAll(prefix.tokenize('.'))
        pathElements.add(replaceInvalidGraphitePathCharacters(jobGroup.name))
        pathElements.add('daily')
        pathElements.add('csi')

        GraphitePathName finalPathName = GraphitePathName.valueOf(pathElements.toArray(new String[pathElements.size()]));
        double valueAsPercentage = mv.csByWptDocCompleteInPercent * 100
        log.debug("Sending ${mv.started}|${valueAsPercentage} as daily shop- CsiAggregation to graphite-path ${finalPathName}")
        socket.sendDate(finalPathName, valueAsPercentage, mv.started)
    }

    private void reportWeeklyPageCsiAggregation(String prefix, JobGroup jobGroup, CsiAggregation mv, GraphiteSocket socket) {
        Page page = mv.page

        List<String> pathElements = []
        pathElements.addAll(prefix.tokenize('.'))
        pathElements.add(replaceInvalidGraphitePathCharacters(jobGroup.name))
        pathElements.add('weekly')
        pathElements.add(replaceInvalidGraphitePathCharacters(page.name))
        pathElements.add('csi')

        GraphitePathName finalPathName = GraphitePathName.valueOf(pathElements.toArray(new String[pathElements.size()]));
        double valueAsPercentage = mv.csByWptDocCompleteInPercent * 100
        log.debug("Sending ${mv.started}|${valueAsPercentage} as weekly page-CsiAggregation to graphite-path ${finalPathName}")
        socket.sendDate(finalPathName, valueAsPercentage, mv.started)
    }

    private void reportWeeklyShopCsiAggregation(String prefix, JobGroup jobGroup, CsiAggregation mv, GraphiteSocket socket) {
        List<String> pathElements = []
        pathElements.addAll(prefix.tokenize('.'))
        pathElements.add(replaceInvalidGraphitePathCharacters(jobGroup.name))
        pathElements.add('weekly')
        pathElements.add('csi')

        GraphitePathName finalPathName = GraphitePathName.valueOf(pathElements.toArray(new String[pathElements.size()]));
        double valueAsPercentage = mv.csByWptDocCompleteInPercent * 100
        log.debug("Sending ${mv.started}|${valueAsPercentage} as weekly shop-CsiAggregation to graphite-path ${finalPathName}")
        socket.sendDate(finalPathName, valueAsPercentage, mv.started)
    }

    private String replaceInvalidGraphitePathCharacters(String graphitePathElement) {
        String replaced = graphitePathElement
        INVALID_GRAPHITE_PATH_CHARACTERS.each { String invalidChar ->
            replaced = replaced.replace(invalidChar, REPLACEMENT_FOR_INVALID_GRAPHITE_PATH_CHARACTERS)
        }
        return replaced
    }
}
