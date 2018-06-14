package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.Measurand
import de.iteratec.osm.result.MeasurandGroup
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.query.EventResultQueryExecutor
import de.iteratec.osm.result.dao.query.projector.MeasurandAverageDataProjector
import de.iteratec.osm.result.dao.query.projector.MeasurandRawDataProjector
import de.iteratec.osm.result.dao.query.projector.UserTimingRawDataProjector
import de.iteratec.osm.result.dao.query.transformer.*
import de.iteratec.osm.util.PerformanceLoggingService
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType

/**
 * Created by mwg on 31.08.2017.
 */
class EventResultQueryBuilder {
    private Integer minValidLoadTime, maxValidLoadTime
    private List<Closure> filters = []
    private Set<ProjectionProperty> baseProjections
    private List<MeasurandTrim> trims = []
    private PerformanceLoggingService performanceLoggingService

    private EventResultQueryExecutor measurandQueryExecutor = new EventResultQueryExecutor()
    private EventResultQueryExecutor userTimingQueryExecutor = new EventResultQueryExecutor()

    EventResultQueryBuilder(Integer minValidLoadtime, Integer maxValidLoadtime) {
        performanceLoggingService = new PerformanceLoggingService()
        filters.add(initBaseClosure())
        minValidLoadTime = minValidLoadtime
        maxValidLoadTime = maxValidLoadtime
        baseProjections = []
    }

    private Closure initBaseClosure() {
        return {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            createAlias('jobResult', 'jobResult')
            createAlias('connectivityProfile', 'connectivityProfile', JoinType.LEFT_OUTER_JOIN)
            between(Measurand.FULLY_LOADED_TIME.eventResultField, minValidLoadTime, maxValidLoadTime)
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
        }
        if (project) {
            baseProjections.add(new ProjectionProperty(dbName: associatedDomainFieldName+'.id', alias: associatedDomainFieldName+ 'Id'))
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
        measurandQueryExecutor.setProjectorAndTransformer(new MeasurandRawDataProjector(), new MeasurandRawDataTransformer())
        userTimingQueryExecutor.setProjectorAndTransformer(new UserTimingRawDataProjector(), new UserTimingRawDataTransformer())
        return getResults()
    }

    List<EventResultProjection> getMedianData(){
        measurandQueryExecutor.setProjectorAndTransformer(new MeasurandRawDataProjector(), new MeasurandMedianDataTransformer(baseProjections: baseProjections, selectedMeasurands: measurandQueryExecutor.selectedMeasurands))
        userTimingQueryExecutor.setProjectorAndTransformer(new UserTimingRawDataProjector(), new UserTimingMedianDataTransformer(baseProjections: baseProjections))
        return getResults()
    }

    List<EventResultProjection> getAverageData(){
        measurandQueryExecutor.setProjectorAndTransformer(new MeasurandAverageDataProjector(), new MeasurandAverageDataTransformer(baseProjections: baseProjections))
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
