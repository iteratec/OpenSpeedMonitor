package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.Measurand
import de.iteratec.osm.result.MeasurandGroup
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.query.EventResultQueryExecutor
import de.iteratec.osm.result.dao.query.MeasurandTrim
import de.iteratec.osm.result.dao.query.ProjectionProperty
import de.iteratec.osm.result.dao.query.TrimQualifier
import de.iteratec.osm.result.dao.query.projector.MeasurandAverageDataProjector
import de.iteratec.osm.result.dao.query.projector.MeasurandRawDataProjector
import de.iteratec.osm.result.dao.query.projector.UserTimingAverageDataProjector
import de.iteratec.osm.result.dao.query.projector.UserTimingRawDataProjector
import de.iteratec.osm.result.dao.query.transformer.*
import de.iteratec.osm.result.dao.query.trimmer.MeasurandAverageDataTrimmer
import de.iteratec.osm.result.dao.query.trimmer.MeasurandRawDataTrimmer
import de.iteratec.osm.result.dao.query.trimmer.UserTimingDataTrimmer
import de.iteratec.osm.util.PerformanceLoggingService
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType

/**
 * Created by mwg on 31.08.2017.
 */
class EventResultQueryBuilder {
    private List<Closure> filters = []
    private Set<ProjectionProperty> baseProjections
    private List<MeasurandTrim> trims = []
    private PerformanceLoggingService performanceLoggingService

    private EventResultQueryExecutor measurandQueryExecutor = new EventResultQueryExecutor()
    private EventResultQueryExecutor userTimingQueryExecutor = new EventResultQueryExecutor()

    EventResultQueryBuilder(Integer minValidLoadtime, Integer maxValidLoadtime) {
        performanceLoggingService = new PerformanceLoggingService()
        filters.add(initBaseClosure())
        baseProjections = []
        trims.add(new MeasurandTrim(onlyForSpecific: Measurand.FULLY_LOADED_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES, value: minValidLoadtime, qualifier: TrimQualifier.GREATER_THAN))
        trims.add(new MeasurandTrim(onlyForSpecific: Measurand.FULLY_LOADED_TIME, measurandGroup: MeasurandGroup.LOAD_TIMES, value: maxValidLoadtime, qualifier: TrimQualifier.LOWER_THAN))
    }

    private Closure initBaseClosure() {
        return {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            createAlias('jobResult', 'jobResult')
            createAlias('connectivityProfile', 'connectivityProfile', JoinType.LEFT_OUTER_JOIN)
            eq('medianValue', true)
        }
    }

    private List<ProjectionProperty> getRichMetaDataProjections() {
        return [
                new ProjectionProperty(dbName: 'id', alias: 'id'),
                new ProjectionProperty(dbName: 'jobResult.wptServerBaseurl', alias: 'wptServerBaseurl'),
                new ProjectionProperty(dbName: 'jobResult.testId', alias: 'testId'),
                new ProjectionProperty(dbName: 'numberOfWptRun', alias: 'numberOfWptRun'),
                new ProjectionProperty(dbName: 'cachedView', alias: 'cachedView'),
                new ProjectionProperty(dbName: 'oneBasedStepIndexInJourney', alias: 'oneBasedStepIndexInJourney'),
                new ProjectionProperty(dbName: 'testDetailsWaterfallURL', alias: 'testDetailsWaterfallURL'),
                new ProjectionProperty(dbName: 'connectivityProfile.name', alias: 'connectivityProfile'),
                new ProjectionProperty(dbName: 'customConnectivityName', alias: 'customConnectivityName'),
                new ProjectionProperty(dbName: 'noTrafficShapingAtAll', alias: 'noTrafficShapingAtAll'),
                new ProjectionProperty(dbName: 'jobResultDate', alias: 'jobResultDate'),
                new ProjectionProperty(dbName: 'testAgent', alias: 'testAgent'),
                new ProjectionProperty(dbName: 'jobGroup.id', alias: 'jobGroupId'),
                new ProjectionProperty(dbName: 'page.id', alias: 'pageId'),
                new ProjectionProperty(dbName: 'measuredEvent.id', alias: 'measuredEventId'),
                new ProjectionProperty(dbName: 'location.id', alias: 'locationId'),
                new ProjectionProperty(dbName: 'browser.id', alias: 'browserId')
        ]
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
        return withAssociatedDomainIdsIn(jobGroupIds, 'jobGroup', project)
    }

    EventResultQueryBuilder withPageIdsIn(List<Long> pageIds, boolean project = true) {
        return withAssociatedDomainIdsIn(pageIds, 'page', project)
    }

    EventResultQueryBuilder withLocationIdsIn(List<Long> locationIds, boolean project = true) {
        return withAssociatedDomainIdsIn(locationIds, 'location', project)
    }

    EventResultQueryBuilder withBrowserIdsIn(List<Long> browserIds, boolean project = true) {
        return withAssociatedDomainIdsIn(browserIds, 'browser', project)
    }

    EventResultQueryBuilder withMeasuredEventIdsIn(List<Long> measuredEventIds, boolean project = true) {
        return withAssociatedDomainIdsIn(measuredEventIds, 'measuredEvent', project)
    }

    EventResultQueryBuilder withJobGroupIn(List<JobGroup> jobGroups, boolean project = true) {
        return withAssociatedDomainIdsIn(jobGroups.collect { it.ident() }, 'jobGroup', project)
    }

    EventResultQueryBuilder withPageIn(List<Page> pages, boolean project = true) {
        return withAssociatedDomainIdsIn(pages.collect { it.ident() }, 'page', project)
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

    EventResultQueryBuilder withProjectedIdForAssociatedDomain(String associatedDomainFieldName) {
        if (associatedDomainFieldName) {
            baseProjections.add(new ProjectionProperty(dbName: associatedDomainFieldName + '.id', alias: associatedDomainFieldName + 'Id'))
        }
        return this
    }

    EventResultQueryBuilder withSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands) {

        measurandQueryExecutor.setMeasurands(selectedMeasurands)
        userTimingQueryExecutor.setUserTimings(selectedMeasurands)

        return this
    }

    List<EventResultProjection> getRawData(boolean withRichMetaData= true) {
        if(withRichMetaData){
            baseProjections.addAll(getRichMetaDataProjections())
        }
        measurandQueryExecutor.setProjector(new MeasurandRawDataProjector())
        measurandQueryExecutor.setTransformer(new MeasurandRawDataTransformer())
        measurandQueryExecutor.setTrimmer(new MeasurandRawDataTrimmer())

        userTimingQueryExecutor.setProjector(new UserTimingRawDataProjector())
        userTimingQueryExecutor.setTransformer(new UserTimingRawDataTransformer(baseProjections: baseProjections))
        userTimingQueryExecutor.setTrimmer(new UserTimingDataTrimmer())
        return getResults()
    }

    List<EventResultProjection> getMedianData(){
        measurandQueryExecutor.setProjector(new MeasurandRawDataProjector())
        measurandQueryExecutor.setTransformer(new MeasurandMedianDataTransformer(baseProjections: baseProjections, selectedMeasurands: measurandQueryExecutor.selectedMeasurands))
        measurandQueryExecutor.setTrimmer(new MeasurandRawDataTrimmer())

        userTimingQueryExecutor.setProjector(new UserTimingRawDataProjector())
        userTimingQueryExecutor.setTransformer(new UserTimingMedianDataTransformer(baseProjections: baseProjections))
        userTimingQueryExecutor.setTrimmer(new UserTimingDataTrimmer())
        return getResults()
    }

    List<EventResultProjection> getAverageData(){
        measurandQueryExecutor.setProjector(new MeasurandAverageDataProjector())
        measurandQueryExecutor.setTransformer(new MeasurandAverageDataTransformer(baseProjections: baseProjections))
        measurandQueryExecutor.setTrimmer(new MeasurandAverageDataTrimmer())

        userTimingQueryExecutor.setProjector(new UserTimingAverageDataProjector())
        userTimingQueryExecutor.setTransformer(new UserTimingAverageDataTransformer(baseProjections: baseProjections))
        userTimingQueryExecutor.setTrimmer(new UserTimingDataTrimmer())

        return getResults()
    }

    private getResults() {
        List<EventResultProjection> userTimingsResult = userTimingQueryExecutor.getResultFor(filters, trims, baseProjections, performanceLoggingService)
        List<EventResultProjection> measurandResult = measurandQueryExecutor.getResultFor(filters, trims, baseProjections, performanceLoggingService)

        List<EventResultProjection> merged
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - merge results', 3) {
            merged = mergeResults(measurandResult, userTimingsResult)
        }

        return merged
    }

    private List<EventResultProjection> mergeResults(List<EventResultProjection> measurandResult, List<EventResultProjection> userTimingResult) {
        if (measurandResult && userTimingResult) {
            measurandResult.each { result ->
                EventResultProjection match = userTimingResult.find { it == result }
                if (match) {
                    result.projectedProperties.putAll(match.projectedProperties)
                }
            }
            return measurandResult
        } else {
            return measurandResult ? measurandResult : userTimingResult
        }

    }
}
