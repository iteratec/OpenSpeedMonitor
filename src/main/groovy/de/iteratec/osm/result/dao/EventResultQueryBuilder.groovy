package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.*
import de.iteratec.osm.result.dao.query.*
import de.iteratec.osm.result.dao.query.projector.MeasurandAverageDataProjector
import de.iteratec.osm.result.dao.query.projector.MeasurandRawDataProjector
import de.iteratec.osm.result.dao.query.projector.UserTimingAverageDataProjector
import de.iteratec.osm.result.dao.query.projector.UserTimingRawDataProjector
import de.iteratec.osm.result.dao.query.transformer.*
import de.iteratec.osm.result.dao.query.trimmer.MeasurandAverageDataTrimmer
import de.iteratec.osm.result.dao.query.trimmer.MeasurandRawDataTrimmer
import de.iteratec.osm.result.dao.query.trimmer.UserTimingDataTrimmer
import de.iteratec.osm.util.PerformanceLoggingService
import de.iteratec.osm.util.PerformanceLoggingService.LogLevel
import de.iteratec.osm.util.PerformanceLoggingService.IndentationDepth
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by mwg on 31.08.2017.
 */
class EventResultQueryBuilder {

    static Logger log = LoggerFactory.getLogger(this)

    private static MIN_DATA_SIZE_FOR_PERCENTILE = 30

    private List<Closure> filters = []
    private Set<ProjectionProperty> baseProjections
    private List<MeasurandTrim> trims = []
    private PerformanceLoggingService performanceLoggingService
    private AspectUtil aspectUtil
    enum MetaDataSet {
        NONE,
        COMPLETE,
        TEST_INFO,
        ASPECT
    }

    private EventResultQueryExecutor measurandQueryExecutor = new EventResultQueryExecutor()
    private EventResultQueryExecutor userTimingQueryExecutor = new EventResultQueryExecutor()

    EventResultQueryBuilder() {
        performanceLoggingService = new PerformanceLoggingService()
        filters.add(initBaseClosure())
        baseProjections = []
        aspectUtil = new AspectUtil()
    }

    private Closure initBaseClosure() {
        return {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            createAlias('jobResult', 'jobResult')
            createAlias('connectivityProfile', 'connectivityProfile', JoinType.LEFT_OUTER_JOIN)
            eq('medianValue', true)
        }
    }

    private List<ProjectionProperty> getMetaDataProjections(MetaDataSet dataSet) {
        switch (dataSet) {
            case MetaDataSet.COMPLETE:
                return [
                        new ProjectionProperty(dbName: 'id', alias: 'id'),
                        new ProjectionProperty(dbName: 'jobResult.wptServerBaseurl', alias: 'wptServerBaseurl'),
                        new ProjectionProperty(dbName: 'jobResult.testId', alias: 'testId'),
                        new ProjectionProperty(dbName: 'numberOfWptRun', alias: 'numberOfWptRun'),
                        new ProjectionProperty(dbName: 'cachedView', alias: 'cachedView'),
                        new ProjectionProperty(dbName: 'oneBasedStepIndexInJourney', alias: 'oneBasedStepIndexInJourney'),
                        new ProjectionProperty(dbName: 'connectivityProfile.name', alias: 'connectivityProfile'),
                        new ProjectionProperty(dbName: 'customConnectivityName', alias: 'customConnectivityName'),
                        new ProjectionProperty(dbName: 'noTrafficShapingAtAll', alias: 'noTrafficShapingAtAll'),
                        new ProjectionProperty(dbName: 'jobResultDate', alias: 'jobResultDate'),
                        new ProjectionProperty(dbName: 'testAgent', alias: 'testAgent'),
                        new ProjectionProperty(dbName: 'jobGroup.id', alias: 'jobGroupId'),
                        new ProjectionProperty(dbName: 'page.id', alias: 'pageId'),
                        new ProjectionProperty(dbName: 'measuredEvent.id', alias: 'measuredEventId'),
                        new ProjectionProperty(dbName: 'location.id', alias: 'locationId'),
                        new ProjectionProperty(dbName: 'browser.id', alias: 'browserId'),
                        new ProjectionProperty(dbName: 'deviceType', alias: 'deviceType'),
                        new ProjectionProperty(dbName: 'operatingSystem', alias: 'operatingSystem')
                ]
            case MetaDataSet.NONE:
                return []
            case MetaDataSet.ASPECT:
                return [
                        new ProjectionProperty(dbName: 'id', alias: 'id'),
                        new ProjectionProperty(dbName: 'jobGroup.id', alias: 'jobGroupId'),
                        new ProjectionProperty(dbName: 'page.id', alias: 'pageId'),
                        new ProjectionProperty(dbName: 'browser.id', alias: 'browserId')
                ]
            case MetaDataSet.TEST_INFO:
                return [
                        new ProjectionProperty(dbName: 'id', alias: 'id'),
                        new ProjectionProperty(dbName: 'jobResult.wptServerBaseurl', alias: 'wptServerBaseurl'),
                        new ProjectionProperty(dbName: 'jobResult.testId', alias: 'testId'),
                        new ProjectionProperty(dbName: 'cachedView', alias: 'cachedView'),
                        new ProjectionProperty(dbName: 'oneBasedStepIndexInJourney', alias: 'oneBasedStepIndexInJourney'),
                        new ProjectionProperty(dbName: 'jobResultDate', alias: 'jobResultDate'),
                        new ProjectionProperty(dbName: 'numberOfWptRun', alias: 'numberOfWptRun')
                ]
            default:
                return []
        }
    }

    EventResultQueryBuilder withJobResultDateBetween(Date from, Date to) {
        if (from && to) {
            filters.add({
                'between' 'jobResultDate', from, to
            })
        }
        return this
    }

    EventResultQueryBuilder withJobGroupIdsIn(List<Long> jobGroupIds, boolean project = true) {
        this.aspectUtil.setJobGroupIds(jobGroupIds)
        return withAssociatedDomainIdsIn(jobGroupIds, 'jobGroup', project)
    }

    EventResultQueryBuilder withPageIdsIn(List<Long> pageIds, boolean project = true) {
        this.aspectUtil.setPageIds(pageIds)
        return withAssociatedDomainIdsIn(pageIds, 'page', project)
    }

    EventResultQueryBuilder withLocationIdsIn(List<Long> locationIds, boolean project = true) {
        return withAssociatedDomainIdsIn(locationIds, 'location', project)
    }

    EventResultQueryBuilder withBrowserIdsIn(List<Long> browserIds, boolean project = true) {
        this.aspectUtil.setBrowserIds(browserIds)
        return withAssociatedDomainIdsIn(browserIds, 'browser', project)
    }

    EventResultQueryBuilder withMeasuredEventIdsIn(List<Long> measuredEventIds, boolean project = true) {
        return withAssociatedDomainIdsIn(measuredEventIds, 'measuredEvent', project)
    }

    EventResultQueryBuilder withOperatingSystems(List<OperatingSystem> operatingSystems, boolean project = true) {
        return withAssociatedDomainIn(operatingSystems, 'operatingSystem', project)
    }

    EventResultQueryBuilder withDeviceTypes(List<DeviceType> deviceTypes, boolean project = true) {
        return withAssociatedDomainIn(deviceTypes, 'deviceType', project)
    }

    EventResultQueryBuilder withJobGroupIn(List<JobGroup> jobGroups, boolean project = true) {
        List<Long> jobGroupIds = jobGroups.collect { it.ident() }
        this.aspectUtil.setJobGroupIds(jobGroupIds)
        return withAssociatedDomainIdsIn(jobGroupIds, 'jobGroup', project)
    }

    EventResultQueryBuilder withPageIn(List<Page> pages, boolean project = true) {
        List<Long> pageIds = pages.collect { it.ident() }
        this.aspectUtil.setPageIds(pageIds)
        return withAssociatedDomainIdsIn(pageIds, 'page', project)
    }

    EventResultQueryBuilder withoutPagesIn(List<Page> pages) {
        List<Long> pageIds = pages.collect { it.ident() }
        this.aspectUtil.removePageIds(pageIds)
        return withAssociatedDomainIdsNotIn(pageIds, 'page')
    }

    EventResultQueryBuilder withPerformanceAspects(List<PerformanceAspectType> aspectTypes) {
        this.aspectUtil.setAspectTypes(aspectTypes)

        if (!measurandQueryExecutor.selectedMeasurands) {
            measurandQueryExecutor.setMeasurands([])
        }

        if (!userTimingQueryExecutor.selectedMeasurands) {
            userTimingQueryExecutor.setUserTimings([])
        }

        return this
    }

    EventResultQueryBuilder withCachedView(CachedView cachedView) {
        filters.add({
            'eq' 'cachedView', cachedView
        })
        return this
    }

    EventResultQueryBuilder withTrim(def trimValue, TrimQualifier trimQualifier, MeasurandGroup measurandGroup) {
        trims.add(new MeasurandTrim(
                measurandGroup: measurandGroup,
                value: trimValue,
                qualifier: trimQualifier
        ))
        return this
    }

    EventResultQueryBuilder withConnectivity(List<Long> connectivityProfileIds, List<String> customConnNames, Boolean includeNativeConnectivity) {
        filters.add({
            or {
                if (connectivityProfileIds) {
                    'in'('connectivityProfile.id', connectivityProfileIds)
                }
                if (customConnNames) {
                    'in'('customConnectivityName', customConnNames)
                }
                if (includeNativeConnectivity) {
                    eq('noTrafficShapingAtAll', true)
                }
            }
        })
        return this
    }

    private EventResultQueryBuilder withAssociatedDomainIdsIn(List<Long> associatedDomainIds, String associatedDomainFieldName, boolean project = true) {
        if (associatedDomainIds) {
            Closure filterClosure = {
                'in' "${associatedDomainFieldName}.id", associatedDomainIds
            }
            filters.add(filterClosure)
            if (project) {
                return withProjectedIdForAssociatedDomain(associatedDomainFieldName)
            }
        }
        return this
    }

    private EventResultQueryBuilder withAssociatedDomainIn(List associatedDomain, String associatedDomainFieldName, boolean project = true) {
        if (associatedDomain) {
            Closure filterClosure = {
                'in' "${associatedDomainFieldName}", associatedDomain
            }
            filters.add(filterClosure)
            if (project) {
                return withProjectedForAssociatedDomain(associatedDomainFieldName)
            }
        }
        return this
    }

    private EventResultQueryBuilder withAssociatedDomainIdsNotIn(List<Long> associatedDomainIds, String associatedDomainFieldName) {
        if (associatedDomainIds) {
            Closure filterClosure = {
                not { 'in' "${associatedDomainFieldName}.id", associatedDomainIds }
            }
            filters.add(filterClosure)
        }
        return this
    }

    EventResultQueryBuilder withProjectedIdForAssociatedDomain(String associatedDomainFieldName) {
        if (associatedDomainFieldName) {
            baseProjections.add(new ProjectionProperty(dbName: associatedDomainFieldName + '.id', alias: associatedDomainFieldName + 'Id'))
        }
        return this
    }

    EventResultQueryBuilder withProjectedForAssociatedDomain(String associatedDomainFieldName) {
        if (associatedDomainFieldName) {
            baseProjections.add(new ProjectionProperty(dbName: associatedDomainFieldName, alias: associatedDomainFieldName))
        }
        return this
    }

    EventResultQueryBuilder withSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands) {

        this.aspectUtil.setMetrics(selectedMeasurands)

        measurandQueryExecutor.setMeasurands(selectedMeasurands)
        userTimingQueryExecutor.setUserTimings(selectedMeasurands)

        return this
    }

    List<EventResultProjection> getRawData(MetaDataSet metaDataSet = MetaDataSet.COMPLETE) {

        if (this.aspectUtil.aspectsIncluded()) {
            this.performanceLoggingService.logExecutionTimeSilently(LogLevel.INFO, 'getRawData - appendAspectMetrics', IndentationDepth.TWO) {
                aspectUtil.appendAspectMetrics(userTimingQueryExecutor.selectedMeasurands, measurandQueryExecutor.selectedMeasurands)
            }
        }

        this.performanceLoggingService.logExecutionTimeSilently(LogLevel.INFO, 'getRawData - preparation', IndentationDepth.TWO) {
            baseProjections.addAll(getMetaDataProjections(metaDataSet))

            measurandQueryExecutor.setProjector(new MeasurandRawDataProjector())
            measurandQueryExecutor.setTransformer(new MeasurandRawDataTransformer())
            measurandQueryExecutor.setTrimmer(new MeasurandRawDataTrimmer())

            userTimingQueryExecutor.setProjector(new UserTimingRawDataProjector())
            userTimingQueryExecutor.setTransformer(new UserTimingRawDataTransformer(baseProjections: baseProjections))
            userTimingQueryExecutor.setTrimmer(new UserTimingDataTrimmer())
        }

        List<EventResultProjection> results = this.performanceLoggingService.logExecutionTimeSilently(
                LogLevel.INFO,
                'getRawData - getting results',
                IndentationDepth.TWO) {
            getResults(false)
        }

        if (this.aspectUtil.aspectsIncluded()) {
            try {
                this.performanceLoggingService.logExecutionTimeSilently(
                        LogLevel.INFO,
                        'getRawData - expandByAspects',
                        IndentationDepth.TWO) {
                    aspectUtil.expandByAspects(results, performanceLoggingService)
                }
            } catch (IllegalStateException e) {
                log.error(e.getMessage(), e)
                return []
            }
        }

        return results
    }

    List<EventResultProjection> getMedianData() {
        if (this.aspectUtil.aspectsIncluded()) {
            List<EventResultProjection> rawData = getRawData(MetaDataSet.ASPECT)
            return this.aspectUtil.getMedianFrom(rawData)
        } else {
            return getMedianDataWithoutAspects()
        }
    }

    List<EventResultProjection> getMedianDataWithoutAspects() {
        measurandQueryExecutor.setProjector(new MeasurandRawDataProjector())
        measurandQueryExecutor.setTransformer(new MeasurandMedianDataTransformer(
                baseProjections: baseProjections,
                selectedMeasurands: measurandQueryExecutor.selectedMeasurands)
        )
        measurandQueryExecutor.setTrimmer(new MeasurandRawDataTrimmer())

        userTimingQueryExecutor.setProjector(new UserTimingRawDataProjector())
        userTimingQueryExecutor.setTransformer(new UserTimingMedianDataTransformer(baseProjections: baseProjections))
        userTimingQueryExecutor.setTrimmer(new UserTimingDataTrimmer())
        return getResults(false)
    }

    List<EventResultProjection> getPercentile(int percentile) {
        if (this.aspectUtil.aspectsIncluded()) {
            List<EventResultProjection> rawData = getRawData(MetaDataSet.ASPECT)
            if (rawData.size() >= MIN_DATA_SIZE_FOR_PERCENTILE) {
                return this.aspectUtil.getPercentileFrom(rawData, percentile)
            }
        } else {
            return getPercentileWithoutAspects(percentile)
        }
        return []
    }

    List<EventResultProjection> getPercentileWithoutAspects(int percentile) {
        measurandQueryExecutor.setProjector(new MeasurandRawDataProjector())
        measurandQueryExecutor.setTransformer(new MeasurandPercentileDataTransformer(percentile, baseProjections, measurandQueryExecutor.selectedMeasurands))
        measurandQueryExecutor.setTrimmer(new MeasurandRawDataTrimmer())

        userTimingQueryExecutor.setProjector(new UserTimingRawDataProjector())
        userTimingQueryExecutor.setTransformer(new UserTimingPercentileDataTransformer(percentile, baseProjections))
        userTimingQueryExecutor.setTrimmer(new UserTimingDataTrimmer())
        return getResults(true)
    }

    List<EventResultProjection> getAverageData() {
        if (this.aspectUtil.aspectsIncluded()) {
            this.performanceLoggingService.resetExecutionTimeLoggingSession()
            List<EventResultProjection> rawData = this.performanceLoggingService.logExecutionTimeSilently(
                    LogLevel.INFO,
                    'getRawData',
                    IndentationDepth.ONE) {
                getRawData(MetaDataSet.ASPECT)
            }
            List<EventResultProjection> avgs = this.performanceLoggingService.logExecutionTimeSilently(
                    LogLevel.INFO,
                    'getAverageFrom',
                    IndentationDepth.ONE) {
                this.aspectUtil.getAverageFrom(rawData)
            }
            log.info(this.performanceLoggingService.getExecutionTimeLoggingSessionData(LogLevel.DEBUG))
            return avgs
        } else {
            return getAverageDataWithoutAspects()
        }
    }

    List<EventResultProjection> getAverageDataWithoutAspects() {
        measurandQueryExecutor.setProjector(new MeasurandAverageDataProjector())
        measurandQueryExecutor.setTransformer(new MeasurandAverageDataTransformer(baseProjections: baseProjections))
        measurandQueryExecutor.setTrimmer(new MeasurandAverageDataTrimmer())

        userTimingQueryExecutor.setProjector(new UserTimingAverageDataProjector())
        userTimingQueryExecutor.setTransformer(new UserTimingAverageDataTransformer(baseProjections: baseProjections))
        userTimingQueryExecutor.setTrimmer(new UserTimingDataTrimmer())

        return getResults(false)
    }

    private List<EventResultProjection> getResults(boolean isMinDataSizeRequired) {
        List<Map> userTimingRawData = []
        List<Map> measurandRawData = []
        List<EventResultProjection> userTimingsResult = []
        List<EventResultProjection> measurandResult = []

        this.performanceLoggingService.logExecutionTimeSilently(
                LogLevel.INFO,
                'getResults - getting user timing results',
                IndentationDepth.THREE) {
            userTimingRawData = userTimingQueryExecutor.getRawResultDataFor(filters, trims, baseProjections, performanceLoggingService)
            userTimingsResult = userTimingQueryExecutor.getResultFor(userTimingRawData, performanceLoggingService)
        }
        this.performanceLoggingService.logExecutionTimeSilently(
                LogLevel.INFO,
                'getResults - getting measurand results',
                IndentationDepth.THREE) {
            measurandRawData = measurandQueryExecutor.getRawResultDataFor(filters, trims, baseProjections, performanceLoggingService)
            measurandResult = measurandQueryExecutor.getResultFor(measurandRawData, performanceLoggingService)
        }

        if (!isMinDataSizeRequired || userTimingRawData.size() + measurandRawData.size() >= MIN_DATA_SIZE_FOR_PERCENTILE) {
            List<EventResultProjection> merged = []
            performanceLoggingService.logExecutionTimeSilently(
                    LogLevel.INFO,
                    "getResults - merge ${measurandResult.size()} measurand results with ${userTimingsResult.size()} user timing results",
                    IndentationDepth.THREE) {
                merged = mergeResults(measurandResult, userTimingsResult)
            }

            return merged
        }
        return []
    }

    private List<EventResultProjection> mergeResults(List<EventResultProjection> measurandResults, List<EventResultProjection> userTimingResults) {
        Map<Object, List<EventResultProjection>> userTimingResultsById = this.performanceLoggingService.logExecutionTimeSilently(
                LogLevel.INFO,
                'mergeResults - group user timings',
                IndentationDepth.FOUR) {
            userTimingResults.groupBy { EventResultProjection erp ->
                erp.id
            }
        }
        return (List<EventResultProjection>) this.performanceLoggingService.logExecutionTimeSilently(
                LogLevel.INFO,
                'mergeResults - group merge user timings into measurands',
                IndentationDepth.FOUR) {
            if (measurandResults && userTimingResults) {
                measurandResults.each { result ->
                    userTimingResultsById[result.id].each { EventResultProjection userTimingResult ->
                        result.projectedProperties.putAll(userTimingResult.projectedProperties)
                    }
                }
                return measurandResults
            } else {
                return measurandResults ? measurandResults : userTimingResults
            }
        }

    }
}
