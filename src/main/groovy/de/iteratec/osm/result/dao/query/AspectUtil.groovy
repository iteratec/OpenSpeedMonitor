package de.iteratec.osm.result.dao.query

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.PerformanceAspect
import de.iteratec.osm.result.PerformanceAspectType
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.dao.EventResultProjection
import org.grails.datastore.mapping.query.api.Criteria

/**
 * Utility for querying {@link PerformanceAspectType} metrics via {@link de.iteratec.osm.result.dao.EventResultQueryBuilderQueryBuilder}.
 * @author nkuhn
 */
class AspectUtil {

    List<Long> jobGroupIds = []
    List<Long> pageIds = []
    List<Long> browserIds = []
    List<PerformanceAspectType> aspectTypes = []

    List<PerformanceAspect> usedAspects = []
    List<SelectedMeasurand> addedMetrics = []

    public boolean aspectsIncluded() {
        return this.aspectTypes.size() > 0
    }

    public void removePageIds(List<Long> pageIds) {
        pageIds.each { this.pageIds.remove(it) }
    }

    /**
     * Appends all metrics we need for PerformanceAspects to the lists that build queries later on. These metrics may
     * come from persisted {@link PerformanceAspect}s or from defaults of queried {@link PerformanceAspectType}s if no
     * aspects are persisted for queried {@link JobGroup}, {@link Page}, {@link Browser} and {@link PerformanceAspectType}.
     *
     * @param userTimings List of user timing metrics that will be queried later on.
     * @param measurands List of measurand metrics that will be queried later on.
     */
    public void appendAspectMetrics(List<SelectedMeasurand> userTimings, List<SelectedMeasurand> measurands) {
        List<PerformanceAspect> aspects = getAspects()
        getQueriedJobGroupIds().each { Long jobGroupId ->
            getQueriedPageIds().each { Long pageId ->
                getQueriedBrowserIds().each { Long browserId ->
                    aspectTypes.each { PerformanceAspectType type ->
                        PerformanceAspect aspect = aspects.find { PerformanceAspect aspect ->
                            aspect.jobGroup.ident() == jobGroupId &&
                                    aspect.page.ident() == pageId &&
                                    aspect.browser.ident() == browserId &&
                                    aspect.performanceAspectType == type
                        }
                        if (aspect) {
                            usedAspects.add(aspect)
                            appendMetricIfMissing(aspect.metric, userTimings, measurands)
                        } else {
                            appendMetricIfMissing(new SelectedMeasurand(type.defaultMetric.toString(), CachedView.UNCACHED), userTimings, measurands)
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds a value for every queried {@link PerformanceAspectType} to the results from db. Originally added aspect metrics
     * remain in the results.
     * @param resultsFromDb
     *              Queried via {@link de.iteratec.osm.result.dao.EventResultQueryBuilderQueryBuilder}.
     * @throws IllegalStateException*              Thrown if results from db doesn't contain id's for {@link JobGroup}, {@link Page}
     *              and {@link Browser} as projected properties. This would be the case if someone uses this class with something
     *              else than raw data from query builder. This isn't supported.
     */
    public void expandByAspectMetrics(List<EventResultProjection> resultsFromDb) throws IllegalStateException {
        aspectTypes.each { PerformanceAspectType type ->
            resultsFromDb.each { EventResultProjection result ->
                Long jobGroupId = result.projectedProperties["jobGroupId"]
                Long pageId = result.projectedProperties["pageId"]
                Long browserId = result.projectedProperties["browserId"]
                if (!jobGroupId || !pageId || !browserId) {
                    throw new IllegalStateException("Result from db doesn't contain jobgroupId, pageId and browserId: ${result}")
                }
                PerformanceAspect usedAspect = usedAspects.find { PerformanceAspect a ->
                    a.jobGroup.ident() == jobGroupId && a.page.ident() == pageId && a.browser.ident() == browserId && a.performanceAspectType == type
                }
                String metricToUse = usedAspect ? usedAspect.metric.getDatabaseRelevantName() : type.defaultMetric.getEventResultField()
                if (result[metricToUse]) {
                    result.projectedProperties[type.toString()] = result[metricToUse]
                }
            }
        }
    }

    private List<PerformanceAspect> getAspects() {
        if (this.aspectTypes.size() == 0) return []
        Criteria criteria = PerformanceAspect.createCriteria()
        List<Closure> filters = [{ 'in'('performanceAspectType', this.aspectTypes) }]

        if (this.jobGroupIds.size() > 0) filters.add({ 'in'('jobGroup.id', getQueriedJobGroupIds()) })
        if (this.pageIds.size() > 0) filters.add({ 'in'('page.id', getQueriedPageIds()) })
        if (this.browserIds.size() > 0) filters.add({ 'in'('browser.id', getQueriedBrowserIds()) })

        return criteria.list {
            filters.each {
                it.delegate = delegate
                it()
            }
        }
    }

    private appendMetricIfMissing(SelectedMeasurand metric, List<SelectedMeasurand> userTimings, List<SelectedMeasurand> measurands) {
        if (metric.selectedType.isUserTiming() && !userTimings.contains(metric)) {
            userTimings.add(metric)
            this.addedMetrics.add(metric.getDatabaseRelevantName())
        } else if (!measurands.contains(metric)) {
            measurands.add(metric)
            this.addedMetrics.add(metric.getDatabaseRelevantName())
        }
    }

    private List<Long> getQueriedJobGroupIds() {
        return this.jobGroupIds ?: JobGroup.list().collect(it.ident())
    }

    private List<Long> getQueriedPageIds() {
        return this.pageIds ?: Page.list().collect(it.ident())
    }

    private List<Long> getQueriedBrowserIds() {
        return this.browserIds ?: Browser.list().collect(it.ident())
    }
}