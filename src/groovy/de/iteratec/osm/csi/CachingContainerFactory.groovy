package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.MeasuredValue
import de.iteratec.osm.report.chart.MeasuredValueInterval
import de.iteratec.osm.result.MeasuredValueTagService
import org.joda.time.DateTime

/**
 *
 * Delivers an instance of {@link MvCachingContainer} for every page {@link de.iteratec.osm.report.chart.MeasuredValue} given when constructed the factory.
 * The caching container is necessary to calculate a {@link de.iteratec.osm.report.chart.MeasuredValue} with the following service method: {@link PageMeasuredValueService#calcMv}
 * @author nkuhn
 * @see PageMeasuredValueService
 *
 */
class CachingContainerFactory{

    MeasuredValueTagService measuredValueTagService
    PageMeasuredValueService pageMeasuredValueService

    Map<String, JobGroup> jobGroupCache = [:]
    Map<String, JobGroup> pageCache = [:]

    List<MeasuredValue> dailyPageMvsToCalculate
    Map<String, List<JobGroup>> dailyJobGroupsByStartDate = [:].withDefault{ new ArrayList<JobGroup>() }
    Map<String, List<Page>> dailyPagesByStartDate = [:].withDefault{ new ArrayList<Page>() }
    Map<String, Map<String, List<MeasuredValue>>> dailyHemvMapByStartDate = [:]

    List<MeasuredValue> weeklyPageMvsToCalculate
    Map<String, List<JobGroup>> weeklyJobGroupsByStartDate = [:].withDefault{ new ArrayList<JobGroup>() }
    Map<String, List<Page>> weeklyPagesByStartDate = [:].withDefault{ new ArrayList<Page>() }
    Map<String, Map<String, List<MeasuredValue>>> weeklyHemvMapByStartDate = [:]

    public CachingContainerFactory(List<MeasuredValue> pmvs, MeasuredValueTagService measuredValueTagService, PageMeasuredValueService pageMeasuredValueService){
        if(pmvs.findAll{ !it.aggregator.name.equals(AggregatorType.PAGE) }.size() > 0) {
            throw new IllegalArgumentException("Class CachingContainerFactory works just with page MeasuredValues!")
        }
        this.pageMeasuredValueService = pageMeasuredValueService
        this.measuredValueTagService = measuredValueTagService
        initialize(pmvs)
    }
    void initialize(List<MeasuredValue> pmvs){
        dailyPageMvsToCalculate = pmvs.findAll{ it.interval.intervalInMinutes ==  MeasuredValueInterval.DAILY}
        weeklyPageMvsToCalculate = pmvs.findAll{ it.interval.intervalInMinutes ==  MeasuredValueInterval.WEEKLY }
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${dailyPageMvsToCalculate.size()} daily page MeasuredValues in initialization of caching container factory.")
        log.info("Quartz controlled cleanup of MeasuredValueUpdateEvents: ${weeklyPageMvsToCalculate.size()} weekly page MeasuredValues in initialization of caching container factory.")

        fillUniqueJobgroupAndPageListsPerStartdate()
        prepareHemvMapsByStartdate()

    }
    MvCachingContainer createContainerFor(MeasuredValue mv){
        return new MvCachingContainer(
                csiGroupToCalcMvFor: jobGroupCache[mv.tag],
                pageToCalcMvFor: pageCache[mv.tag],
                hmvsByCsiGroupPageCombination: this.getHemvMapOf(mv))
    }
    Map<String,List<MeasuredValue>> getHemvMapOf(MeasuredValue mv){
        if(mv.interval.intervalInMinutes == MeasuredValueInterval.DAILY) return dailyHemvMapByStartDate[mv.started.toString()]
        else if(mv.interval.intervalInMinutes == MeasuredValueInterval.WEEKLY) return weeklyHemvMapByStartDate[mv.started.toString()]
        else throw new IllegalArgumentException("Page MeasuredValues can only have interval DAILY or WEEKLY! This MeasuredValue caused this Exception: ${mv}")
    }
    void fillUniqueJobgroupAndPageListsPerStartdate(){
        dailyPageMvsToCalculate.each {dpmv ->

            JobGroup jobGroup = jobGroupCache[dpmv.tag]
            if( jobGroup == null){
                jobGroup = measuredValueTagService.findJobGroupOfWeeklyPageTag(dpmv.tag)
                jobGroupCache[dpmv.tag] = jobGroup
            }
            dailyJobGroupsByStartDate[dpmv.started.toString()].add(jobGroup)

            Page page = pageCache[dpmv.tag]
            if(page == null){
                page = measuredValueTagService.findPageOfWeeklyPageTag(dpmv.tag)
                pageCache[dpmv.tag] = page
            }
            dailyPagesByStartDate[dpmv.started.toString()].add(page)

        }
        weeklyPageMvsToCalculate.each {wpmv ->

            JobGroup jobGroup = jobGroupCache[wpmv.tag]
            if( jobGroup == null){
                jobGroup = measuredValueTagService.findJobGroupOfWeeklyPageTag(wpmv.tag)
                jobGroupCache[wpmv.tag] = jobGroup
            }
            weeklyJobGroupsByStartDate[wpmv.started.toString()].add(jobGroup)

            Page page = pageCache[wpmv.tag]
            if(page == null){
                page = measuredValueTagService.findPageOfWeeklyPageTag(wpmv.tag)
                pageCache[wpmv.tag] = page
            }
            weeklyPagesByStartDate[wpmv.started.toString()].add(page)

        }
    }
    void prepareHemvMapsByStartdate(){
        dailyPageMvsToCalculate*.started.unique().each{Date uniqueStartDate ->

            DateTime startForGettingHemv = new DateTime(uniqueStartDate)
            String uniqueStartDateAsString = uniqueStartDate.toString()

            dailyHemvMapByStartDate[uniqueStartDateAsString] = pageMeasuredValueService.getHmvsByCsiGroupPageCombinationMap(
                    dailyJobGroupsByStartDate[uniqueStartDateAsString].unique(),
                    dailyPagesByStartDate[uniqueStartDateAsString].unique(),
                    startForGettingHemv,
                    startForGettingHemv.plusMinutes(MeasuredValueInterval.DAILY))
        }
        weeklyPageMvsToCalculate*.started.unique().each{Date uniqueStartDate ->

            DateTime startForGettingHemv = new DateTime(uniqueStartDate)
            String uniqueStartDateAsString = uniqueStartDate.toString()

            weeklyHemvMapByStartDate[uniqueStartDateAsString] = pageMeasuredValueService.getHmvsByCsiGroupPageCombinationMap(
                    weeklyJobGroupsByStartDate[uniqueStartDateAsString].unique(),
                    weeklyPagesByStartDate[uniqueStartDateAsString].unique(),
                    startForGettingHemv,
                    startForGettingHemv.plusMinutes(MeasuredValueInterval.WEEKLY))
        }
    }
}
