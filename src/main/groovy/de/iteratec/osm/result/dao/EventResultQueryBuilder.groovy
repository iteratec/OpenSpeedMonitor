package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.Page
import de.iteratec.osm.dao.ProjectionProperty
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.MeasurandGroup
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.util.PerformanceLoggingService
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType

/**
 * Created by mwg on 31.08.2017.
 */
class EventResultQueryBuilder {

    private List<Closure> filters = []
    private List<ProjectionProperty> baseProjections
    private List<MeasurandTrim> trims = []
    private PerformanceLoggingService performanceLoggingService

    private SelectedMeasurandQueryBuilder measurandRawQueryBuilder, userTimingRawQueryBuilder

    EventResultQueryBuilder(Integer minValidLoadtime, Integer maxValidLoadtime) {
        performanceLoggingService = new PerformanceLoggingService()
        filters.add(initBaseClosure(minValidLoadtime, maxValidLoadtime))
        baseProjections = initBaseProjections()
    }

    private Closure initBaseClosure(Integer minValidLoadtime, Integer maxValidLoadtime) {
        return {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            createAlias('jobResult', 'jobResult')
            createAlias('connectivityProfile', 'connectivityProfile', JoinType.LEFT_OUTER_JOIN)
            'between'('fullyLoadedTimeInMillisecs', minValidLoadtime, maxValidLoadtime)
            eq('medianValue', true)
        }
    }

    private List<ProjectionProperty> initBaseProjections() {
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

    EventResultQueryBuilder withJobGroupIdsIn(List<Long> jobGroupIds, boolean project = false) {
        return withAssociatedDomainIdsIn(jobGroupIds, 'jobGroup', project)
    }

    EventResultQueryBuilder withPageIdsIn(List<Long> pageIds, boolean project = false) {
        return withAssociatedDomainIdsIn(pageIds, 'page', project)
    }

    EventResultQueryBuilder withLocationIdsIn(List<Long> locationIds, boolean project = false) {
        return withAssociatedDomainIdsIn(locationIds, 'location', project)
    }

    EventResultQueryBuilder withBrowserIdsIn(List<Long> browserIds, boolean project = false) {
        return withAssociatedDomainIdsIn(browserIds, 'browser', project)
    }

    EventResultQueryBuilder withMeasuredEventIdsIn(List<Long> measuredEventIds, boolean project = false) {
        return withAssociatedDomainIdsIn(measuredEventIds, 'measuredEvent', project)
    }

    EventResultQueryBuilder withJobGroupIn(List<JobGroup> jobGroups, boolean project = false) {
        return withAssociatedDomainIdsIn(jobGroups.collect { it.ident() }, 'jobGroup', project)
    }

    EventResultQueryBuilder withPageIn(List<Page> pages, boolean project = false) {
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
            filters.add({
                'in' "${associatedDomainFieldName}.id", associatedDomainIds
            })
        }
        if (project) {
            baseProjections.add(new ProjectionProperty(dbName: associatedDomainFieldName, alias: associatedDomainFieldName))
        }
        return this
    }

    EventResultQueryBuilder withSelectedMeasurands(List<SelectedMeasurand> selectedMeasurands) {
        List<SelectedMeasurand> measurands = selectedMeasurands.findAll { !it.selectedType.isUserTiming() }
        List<SelectedMeasurand> userTimings = selectedMeasurands.findAll { it.selectedType.isUserTiming() }

        if (measurands) {
            initMeasurandsQueryBuilder()
            measurandRawQueryBuilder.configureForSelectedMeasurands(measurands)
        }
        if (userTimings) {
            initUserTimingsQueryBuilder()
            userTimingRawQueryBuilder.configureForSelectedMeasurands(userTimings)
        }

        return this
    }

    List<EventResultProjection> getRawData() {
        return getResultFor(userTimingRawQueryBuilder, measurandRawQueryBuilder)
    }

    private getResultFor(SelectedMeasurandQueryBuilder userTimingsBuilder, SelectedMeasurandQueryBuilder measurandsBuilder) {
        List<EventResultProjection> userTimingsResult = []
        List<EventResultProjection> measurandResult = []

        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - get usertiming results', 3) {
            if (userTimingsBuilder) {
                userTimingsResult += userTimingsBuilder.getResultsForFilter(filters, baseProjections, trims, performanceLoggingService)
            }
        }
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, 'getting event-results - get measurand results', 3) {
            if (measurandsBuilder) {
                measurandResult += measurandsBuilder.getResultsForFilter(filters, baseProjections, trims, performanceLoggingService)
            }
        }

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

    private initUserTimingsQueryBuilder() {
        if (!userTimingRawQueryBuilder) {
            userTimingRawQueryBuilder = new UserTimingRawDataQueryBuilder()
        }
    }

    private initMeasurandsQueryBuilder() {
        if (!measurandRawQueryBuilder) {
            measurandRawQueryBuilder = new MeasurandRawDataQueryBuilder()
        }
    }
}
