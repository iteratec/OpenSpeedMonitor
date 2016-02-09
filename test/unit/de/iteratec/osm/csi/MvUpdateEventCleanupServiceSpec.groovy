package de.iteratec.osm.csi

import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.MeasuredValueTagService
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Specification

/**
 * Created by nkuhn on 22.05.15.
 */
@TestFor(MvUpdateEventCleanupService)
@Mock([CsiAggregation, MeasuredValueInterval, AggregatorType, BatchActivity, MeasuredValueUpdateEvent, JobGroup, Page])
class MvUpdateEventCleanupServiceSpec extends Specification {

    MvUpdateEventCleanupService serviceUnderTest

    private static long idDailyPageMvInitiallyOpenAndCalculated
    private static long idDailyPageMvInitiallyOpenAndOutdated

    private static long idDailyShopMvInitiallyOpenAndCalculated
    private static long idDailyShopMvInitiallyOpenAndOutdated

    private static long idWeeklyPageMvInitiallyOpenAndCalculated
    private static long idWeeklyPageMvInitiallyOpenAndOutdated

    private static long idWeeklyShopMvInitiallyOpenAndCalculated
    private static long idWeeklyShopMvInitiallyOpenAndOutdated

    /**
     * This map contains id's of tested MeasuredValues as keys and the number these MeasuredValues get calculated in
     * respective service method as values. Counter values get incremented in mocked service methods.
     */
    private static Map calculationCounts = [:].withDefault { 0 }

    private static final String irrelevant_PageTag = '1;1'
    private static final String irrelevant_ShopTag = '1'
    private static final Date irrelevant_MeasuredValueDate = new Date()
    private static final Double irrelevant_Value = 42d
    private static final String irrelevant_ResultIds = '1,2,3'

    MeasuredValueInterval daily
    MeasuredValueInterval weekly
    AggregatorType page
    AggregatorType shop

    void setup() {
        serviceUnderTest = service
        createTestDataCommonForAllTests()
        addMocksCommonForAllTests()
    }

    void "already calculated daily page mvs get closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idDailyPageMvInitiallyOpenAndCalculated])

        when:
        assert MeasuredValueUpdateEvent.findAllByMeasuredValueId(idDailyPageMvInitiallyOpenAndCalculated).size() == 1
        assert CsiAggregation.get(idDailyPageMvInitiallyOpenAndCalculated).closedAndCalculated == false
        serviceUnderTest.closeMeasuredValuesExpiredForAtLeast(300)

        then:
        calculationCounts[idDailyPageMvInitiallyOpenAndCalculated] == 0
        MeasuredValueUpdateEvent.findAllByMeasuredValueId(idDailyPageMvInitiallyOpenAndCalculated).size() == 0
        CsiAggregation.get(idDailyPageMvInitiallyOpenAndCalculated).closedAndCalculated == true
    }

    void "outdated daily page mvs get calculated and closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idDailyPageMvInitiallyOpenAndOutdated])

        when:
        assert MeasuredValueUpdateEvent.findAllByMeasuredValueId(idDailyPageMvInitiallyOpenAndOutdated).size() == 1
        assert CsiAggregation.get(idDailyPageMvInitiallyOpenAndOutdated).closedAndCalculated == false
        serviceUnderTest.closeMeasuredValuesExpiredForAtLeast(300)

        then:
        calculationCounts[idDailyPageMvInitiallyOpenAndOutdated] == 1
        MeasuredValueUpdateEvent.findAllByMeasuredValueId(idDailyPageMvInitiallyOpenAndOutdated).size() == 0
        CsiAggregation.get(idDailyPageMvInitiallyOpenAndOutdated).closedAndCalculated == true
    }

    void "already calculated weekly page mvs get closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idWeeklyPageMvInitiallyOpenAndCalculated])

        when:
        assert MeasuredValueUpdateEvent.findAllByMeasuredValueId(idWeeklyPageMvInitiallyOpenAndCalculated).size() == 1
        assert CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndCalculated).closedAndCalculated == false
        serviceUnderTest.closeMeasuredValuesExpiredForAtLeast(300)

        then:
        calculationCounts[idWeeklyPageMvInitiallyOpenAndCalculated] == 0
        MeasuredValueUpdateEvent.findAllByMeasuredValueId(idWeeklyPageMvInitiallyOpenAndCalculated).size() == 0
        CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndCalculated).closedAndCalculated == true
    }

    void "outdated weekly page mvs get calculated and closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idWeeklyPageMvInitiallyOpenAndOutdated])

        when:
        assert MeasuredValueUpdateEvent.findAllByMeasuredValueId(idWeeklyPageMvInitiallyOpenAndOutdated).size() == 1
        assert CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndOutdated).closedAndCalculated == false
        serviceUnderTest.closeMeasuredValuesExpiredForAtLeast(300)

        then:
        calculationCounts[idWeeklyPageMvInitiallyOpenAndOutdated] == 1
        MeasuredValueUpdateEvent.findAllByMeasuredValueId(idWeeklyPageMvInitiallyOpenAndOutdated).size() == 0
        CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndOutdated).closedAndCalculated == true
    }

    void "already calculated daily shop mvs get closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idDailyShopMvInitiallyOpenAndCalculated])

        when:
        assert MeasuredValueUpdateEvent.findAllByMeasuredValueId(idDailyShopMvInitiallyOpenAndCalculated).size() == 1
        assert CsiAggregation.get(idDailyShopMvInitiallyOpenAndCalculated).closedAndCalculated == false
        serviceUnderTest.closeMeasuredValuesExpiredForAtLeast(300)

        then:
        calculationCounts[idDailyShopMvInitiallyOpenAndCalculated] == 0
        MeasuredValueUpdateEvent.findAllByMeasuredValueId(idDailyShopMvInitiallyOpenAndCalculated).size() == 0
        CsiAggregation.get(idDailyShopMvInitiallyOpenAndCalculated).closedAndCalculated == true
    }

    void "outdated daily shop mvs get calculated and closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idDailyShopMvInitiallyOpenAndOutdated])

        when:
        assert MeasuredValueUpdateEvent.findAllByMeasuredValueId(idDailyShopMvInitiallyOpenAndOutdated).size() == 1
        assert CsiAggregation.get(idDailyShopMvInitiallyOpenAndOutdated).closedAndCalculated == false
        serviceUnderTest.closeMeasuredValuesExpiredForAtLeast(300)

        then:
        calculationCounts[idDailyShopMvInitiallyOpenAndOutdated] == 1
        MeasuredValueUpdateEvent.findAllByMeasuredValueId(idDailyShopMvInitiallyOpenAndOutdated).size() == 0
        CsiAggregation.get(idDailyShopMvInitiallyOpenAndOutdated).closedAndCalculated == true
    }

    void "already calculated weekly shop mvs get closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idWeeklyShopMvInitiallyOpenAndCalculated])

        when:
        assert MeasuredValueUpdateEvent.findAllByMeasuredValueId(idWeeklyShopMvInitiallyOpenAndCalculated).size() == 1
        assert CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndCalculated).closedAndCalculated == false
        serviceUnderTest.closeMeasuredValuesExpiredForAtLeast(300)

        then:
        calculationCounts[idWeeklyShopMvInitiallyOpenAndCalculated] == 0
        MeasuredValueUpdateEvent.findAllByMeasuredValueId(idWeeklyShopMvInitiallyOpenAndCalculated).size() == 0
        CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndCalculated).closedAndCalculated == true
    }

    void "outdated weekly shop mvs get calculated and closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idWeeklyShopMvInitiallyOpenAndOutdated])

        when:
        assert MeasuredValueUpdateEvent.findAllByMeasuredValueId(idWeeklyShopMvInitiallyOpenAndOutdated).size() == 1
        assert CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndOutdated).closedAndCalculated == false
        serviceUnderTest.closeMeasuredValuesExpiredForAtLeast(300)

        then:
        calculationCounts[idWeeklyShopMvInitiallyOpenAndOutdated] == 1
        MeasuredValueUpdateEvent.findAllByMeasuredValueId(idWeeklyShopMvInitiallyOpenAndOutdated).size() == 0
        CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndOutdated).closedAndCalculated == true
    }

    /**
     * Resets all calculation counters to 0.
     */
    private void resetCalculationCounts() {
        calculationCounts = [:].withDefault { 0 }
    }

    /**
     * Mocks methods {@link MeasuredValueDaoService#getOpenMeasuredValuesWhosIntervalExpiredForAtLeast} and
     * {@link MeasuredValueDaoService#getUpdateEvents} to return {@link CsiAggregation}s of given id list and associated
     * {@link MeasuredValueUpdateEvent}s.
     * @param mvIds List of id's of {@link MeasuredValueUpdateEvent}s to return from mocked method.
     */
    private void prepareDaoServiceMock(List<Long> mvIds) {
        serviceUnderTest.measuredValueDaoService = [
                getOpenMeasuredValuesWhosIntervalExpiredForAtLeast: { int minutes ->
                    return mvIds.collect { CsiAggregation.get(it) }
                },
                getUpdateEvents                                   : { List<Long> measuredValueIds ->
                    return mvIds.inject([]) { List<MeasuredValueUpdateEvent> updateEvents, Long mvId ->
                        updateEvents.addAll(MeasuredValueUpdateEvent.findAllByMeasuredValueId(mvId))
                        return updateEvents
                    }
                }
        ] as MeasuredValueDaoService
    }

    void createTestDataCommonForAllTests() {

        List<MeasuredValueInterval> intervals = TestDataUtil.createMeasuredValueIntervals()
        List<AggregatorType> aggregators = TestDataUtil.createAggregatorTypes()
        daily = intervals.find { it.intervalInMinutes == MeasuredValueInterval.DAILY }
        weekly = intervals.find { it.intervalInMinutes == MeasuredValueInterval.WEEKLY }
        page = aggregators.find { it.name.equals(AggregatorType.PAGE) }
        shop = aggregators.find { it.name.equals(AggregatorType.SHOP) }

        createMeasuredValues()

        createUpdateEvents()

    }

    void createMeasuredValues() {

        CsiAggregation mvDailyPageCalculated = TestDataUtil.createMeasuredValue(irrelevant_MeasuredValueDate, daily, page, irrelevant_PageTag, irrelevant_Value, irrelevant_ResultIds, false)
        idDailyPageMvInitiallyOpenAndCalculated = mvDailyPageCalculated.ident()
        CsiAggregation mvDailyPageOutdated = TestDataUtil.createMeasuredValue(irrelevant_MeasuredValueDate, daily, page, irrelevant_PageTag, irrelevant_Value, irrelevant_ResultIds, false)
        idDailyPageMvInitiallyOpenAndOutdated = mvDailyPageOutdated.ident()

        CsiAggregation mvDailyShopCalculated = TestDataUtil.createMeasuredValue(irrelevant_MeasuredValueDate, daily, shop, irrelevant_ShopTag, irrelevant_Value, irrelevant_ResultIds, false)
        idDailyShopMvInitiallyOpenAndCalculated = mvDailyShopCalculated.ident()
        CsiAggregation mvDailyShopOutdated = TestDataUtil.createMeasuredValue(irrelevant_MeasuredValueDate, daily, shop, irrelevant_ShopTag, irrelevant_Value, irrelevant_ResultIds, false)
        idDailyShopMvInitiallyOpenAndOutdated = mvDailyShopOutdated.ident()

        CsiAggregation mvWeeklyPageCalculated = TestDataUtil.createMeasuredValue(irrelevant_MeasuredValueDate, weekly, page, irrelevant_PageTag, irrelevant_Value, irrelevant_ResultIds, false)
        idWeeklyPageMvInitiallyOpenAndCalculated = mvWeeklyPageCalculated.ident()
        CsiAggregation mvWeeklyPageOutdated = TestDataUtil.createMeasuredValue(irrelevant_MeasuredValueDate, weekly, page, irrelevant_PageTag, irrelevant_Value, irrelevant_ResultIds, false)
        idWeeklyPageMvInitiallyOpenAndOutdated = mvWeeklyPageOutdated.ident()

        CsiAggregation mvWeeklyShopCalculated = TestDataUtil.createMeasuredValue(irrelevant_MeasuredValueDate, weekly, shop, irrelevant_PageTag, irrelevant_Value, irrelevant_ResultIds, false)
        idWeeklyShopMvInitiallyOpenAndCalculated = mvWeeklyShopCalculated.ident()
        CsiAggregation mvWeeklyShopOutdated = TestDataUtil.createMeasuredValue(irrelevant_MeasuredValueDate, weekly, shop, irrelevant_PageTag, irrelevant_Value, irrelevant_ResultIds, false)
        idWeeklyShopMvInitiallyOpenAndOutdated = mvWeeklyShopOutdated.ident()
    }

    void createUpdateEvents() {
        TestDataUtil.createUpdateEvent(idDailyPageMvInitiallyOpenAndCalculated, MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
        TestDataUtil.createUpdateEvent(idDailyPageMvInitiallyOpenAndOutdated, MeasuredValueUpdateEvent.UpdateCause.OUTDATED)

        TestDataUtil.createUpdateEvent(idDailyShopMvInitiallyOpenAndCalculated, MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
        TestDataUtil.createUpdateEvent(idDailyShopMvInitiallyOpenAndOutdated, MeasuredValueUpdateEvent.UpdateCause.OUTDATED)

        TestDataUtil.createUpdateEvent(idWeeklyPageMvInitiallyOpenAndCalculated, MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
        TestDataUtil.createUpdateEvent(idWeeklyPageMvInitiallyOpenAndOutdated, MeasuredValueUpdateEvent.UpdateCause.OUTDATED)

        TestDataUtil.createUpdateEvent(idWeeklyShopMvInitiallyOpenAndCalculated, MeasuredValueUpdateEvent.UpdateCause.CALCULATED)
        TestDataUtil.createUpdateEvent(idWeeklyShopMvInitiallyOpenAndOutdated, MeasuredValueUpdateEvent.UpdateCause.OUTDATED)
    }

    void addMocksCommonForAllTests() {

        serviceUnderTest.batchActivityService = [
                getActiveBatchActivity: { Class c, long idWithinDomain, Activity activity, String name, boolean observe = true ->
                    return [updateStatus: { Map<String, Object> map -> /*do nothing*/ }] as BatchActivity
                },
                calculateProgress     : { int count, int actual -> 'not the concern of these tests' }
        ] as BatchActivityService

        serviceUnderTest.inMemoryConfigService.metaClass {
            areMeasurementsGenerallyEnabled { -> return true }
        }

        serviceUnderTest.measuredValueTagService = new MeasuredValueTagService()

        serviceUnderTest.shopMeasuredValueService = [
                calcMv: { CsiAggregation toBeCalculated ->
                    calculationCounts[toBeCalculated.ident()] = ++calculationCounts[toBeCalculated.ident()]
                    return null
                }
        ] as ShopMeasuredValueService

        serviceUnderTest.pageMeasuredValueService = [
                getHmvsByCsiGroupPageCombinationMap: { List<JobGroup> csiGroups, List<Page> csiPages, DateTime startDateTime, DateTime endDateTime ->
                    Map irrelevantBecauseWholeCalculationIsMocked = [:]
                    return irrelevantBecauseWholeCalculationIsMocked
                },
                calcMv                             : { CsiAggregation toBeCalculated, MvCachingContainer cachingContainer ->
                    calculationCounts[toBeCalculated.ident()] = ++calculationCounts[toBeCalculated.ident()]
                    return null
                }
        ] as PageMeasuredValueService


        serviceUnderTest.measuredValueTagService = [
                getAllPagesFromWeeklyOrDailyPageTags    : { unused ->
                    def result = [:].withDefault { [] }
                    result[1].add(new Page())
                    return result
                },
                getAllJobGroupsFromWeeklyOrDailyPageTags: { unused ->
                    def result = [:].withDefault { [] }
                    result[1].add(new JobGroup())
                    return result
                }
        ] as MeasuredValueTagService

        mockCachingContainerService()
    }

    void mockCachingContainerService() {
        ServiceMocker mocker = ServiceMocker.create()

        def returnForGetDailyHemvMapByStartDate = [:].withDefault { [:].withDefault { [] } }
        def returnForGetWeeklyHemvMapByStartDate = [:].withDefault { [:].withDefault { [] } }
        def returnForGetDailyJobGroupsByStartDate = [:].withDefault { [] }
        def returnForGetDailyPagesByStartDate = [:].withDefault { [] }
        def returnForGetWeeklyJobGroupsByStartDate = [:].withDefault { [] }
        def returnForGetWeeklyPagesByStartDate = [:].withDefault { [] }
        def page = new Page(id: 1)
        def hmvsByCsiGroupPageCombination = [:].withDefault {[]}
        hmvsByCsiGroupPageCombination.put(CsiAggregation.get(idDailyShopMvInitiallyOpenAndOutdated).started.toString(), MeasuredValueUpdateEvent.findAllByMeasuredValueId(idDailyShopMvInitiallyOpenAndOutdated))
        def returnForCreateContainerFor = new MvCachingContainer(csiGroupToCalcMvFor: null,
                pageToCalcMvFor: page,
                hmvsByCsiGroupPageCombination: hmvsByCsiGroupPageCombination)

        mocker.mockCachingContainerService(serviceUnderTest, returnForGetDailyJobGroupsByStartDate, returnForGetDailyPagesByStartDate,
                returnForGetDailyHemvMapByStartDate, returnForGetWeeklyJobGroupsByStartDate, returnForGetWeeklyPagesByStartDate,
                returnForGetWeeklyHemvMapByStartDate, returnForCreateContainerFor)
    }
}
