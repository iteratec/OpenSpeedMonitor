package de.iteratec.osm.csi

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.result.CsiAggregationTagService
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.mock.interceptor.StubFor
import org.joda.time.DateTime
import spock.lang.Specification

/**
 * Created by nkuhn on 22.05.15.
 */
@TestFor(CsiAggregationUpdateEventCleanupService)
@Mock([CsiAggregation, CsiAggregationInterval, AggregatorType, BatchActivity, CsiAggregationUpdateEvent, JobGroup, Page])
class CsiAggregationUpdateEventCleanupServiceSpec extends Specification {

    CsiAggregationUpdateEventCleanupService serviceUnderTest

    private static long idDailyPageMvInitiallyOpenAndCalculated
    private static long idDailyPageMvInitiallyOpenAndOutdated

    private static long idDailyShopMvInitiallyOpenAndCalculated
    private static long idDailyShopMvInitiallyOpenAndOutdated

    private static long idWeeklyPageMvInitiallyOpenAndCalculated
    private static long idWeeklyPageMvInitiallyOpenAndOutdated

    private static long idWeeklyShopMvInitiallyOpenAndCalculated
    private static long idWeeklyShopMvInitiallyOpenAndOutdated

    /**
     * This map contains id's of tested CsiAggregations as keys and the number these CsiAggregations get calculated in
     * respective service method as values. Counter values get incremented in mocked service methods.
     */
    private static Map calculationCounts = [:].withDefault { 0 }

    private static final String irrelevant_PageTag = '1;1'
    private static final String irrelevant_ShopTag = '1'
    private static final Date irrelevant_CsiAggregationDate = new Date()
    private static final Double irrelevant_Value = 42d
    private static final String irrelevant_ResultIds = '1,2,3'

    CsiAggregationInterval daily
    CsiAggregationInterval weekly
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
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndCalculated).size() == 1
        assert CsiAggregation.get(idDailyPageMvInitiallyOpenAndCalculated).closedAndCalculated == false
        serviceUnderTest.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        calculationCounts[idDailyPageMvInitiallyOpenAndCalculated] == 0
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndCalculated).size() == 0
        CsiAggregation.get(idDailyPageMvInitiallyOpenAndCalculated).closedAndCalculated == true
    }

    void "outdated daily page mvs get calculated and closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idDailyPageMvInitiallyOpenAndOutdated])

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndOutdated).size() == 1
        assert CsiAggregation.get(idDailyPageMvInitiallyOpenAndOutdated).closedAndCalculated == false
        serviceUnderTest.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        calculationCounts[idDailyPageMvInitiallyOpenAndOutdated] == 1
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndOutdated).size() == 0
        CsiAggregation.get(idDailyPageMvInitiallyOpenAndOutdated).closedAndCalculated == true
    }

    void "already calculated weekly page mvs get closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idWeeklyPageMvInitiallyOpenAndCalculated])

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndCalculated).size() == 1
        assert CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndCalculated).closedAndCalculated == false
        serviceUnderTest.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        calculationCounts[idWeeklyPageMvInitiallyOpenAndCalculated] == 0
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndCalculated).size() == 0
        CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndCalculated).closedAndCalculated == true
    }

    void "outdated weekly page mvs get calculated and closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idWeeklyPageMvInitiallyOpenAndOutdated])

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndOutdated).size() == 1
        assert CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndOutdated).closedAndCalculated == false
        serviceUnderTest.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        calculationCounts[idWeeklyPageMvInitiallyOpenAndOutdated] == 1
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndOutdated).size() == 0
        CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndOutdated).closedAndCalculated == true
    }

    void "already calculated daily shop mvs get closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idDailyShopMvInitiallyOpenAndCalculated])

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndCalculated).size() == 1
        assert CsiAggregation.get(idDailyShopMvInitiallyOpenAndCalculated).closedAndCalculated == false
        serviceUnderTest.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        calculationCounts[idDailyShopMvInitiallyOpenAndCalculated] == 0
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndCalculated).size() == 0
        CsiAggregation.get(idDailyShopMvInitiallyOpenAndCalculated).closedAndCalculated == true
    }

    void "outdated daily shop mvs get calculated and closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idDailyShopMvInitiallyOpenAndOutdated])

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndOutdated).size() == 1
        assert CsiAggregation.get(idDailyShopMvInitiallyOpenAndOutdated).closedAndCalculated == false
        serviceUnderTest.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        calculationCounts[idDailyShopMvInitiallyOpenAndOutdated] == 1
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndOutdated).size() == 0
        CsiAggregation.get(idDailyShopMvInitiallyOpenAndOutdated).closedAndCalculated == true
    }

    void "already calculated weekly shop mvs get closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idWeeklyShopMvInitiallyOpenAndCalculated])

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndCalculated).size() == 1
        assert CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndCalculated).closedAndCalculated == false
        serviceUnderTest.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        calculationCounts[idWeeklyShopMvInitiallyOpenAndCalculated] == 0
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndCalculated).size() == 0
        CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndCalculated).closedAndCalculated == true
    }

    void "outdated weekly shop mvs get calculated and closed"() {
        setup:
        resetCalculationCounts()
        prepareDaoServiceMock([idWeeklyShopMvInitiallyOpenAndOutdated])

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndOutdated).size() == 1
        assert CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndOutdated).closedAndCalculated == false
        serviceUnderTest.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        calculationCounts[idWeeklyShopMvInitiallyOpenAndOutdated] == 1
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndOutdated).size() == 0
        CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndOutdated).closedAndCalculated == true
    }

    /**
     * Resets all calculation counters to 0.
     */
    private void resetCalculationCounts() {
        calculationCounts = [:].withDefault { 0 }
    }

    /**
     * Mocks methods {@link CsiAggregationDaoService#getOpenCsiAggregationsWhosIntervalExpiredForAtLeast} and
     * {@link CsiAggregationDaoService#getUpdateEvents} to return {@link CsiAggregation}s of given id list and associated
     * {@link CsiAggregationUpdateEvent}s.
     * @param mvIds List of id's of {@link CsiAggregationUpdateEvent}s to return from mocked method.
     */
    private void prepareDaoServiceMock(List<Long> mvIds) {
        def csiAggregationDaoService = new StubFor(CsiAggregationDaoService)

        csiAggregationDaoService.demand.getOpenCsiAggregationsWhosIntervalExpiredForAtLeast { int minutes ->
            return mvIds.collect { CsiAggregation.get(it) }
        }
        csiAggregationDaoService.demand.getUpdateEvents { List<Long> csiAggregationValueIds ->
            return mvIds.inject([]) { List<CsiAggregationUpdateEvent> updateEvents, Long mvId ->
                updateEvents.addAll(CsiAggregationUpdateEvent.findAllByCsiAggregationId(mvId))
                return updateEvents
            }
        }

        serviceUnderTest.csiAggregationDaoService = csiAggregationDaoService.proxyInstance()
    }

    void createTestDataCommonForAllTests() {

        List<CsiAggregationInterval> intervals = TestDataUtil.createCsiAggregationIntervals()
        List<AggregatorType> aggregators = TestDataUtil.createAggregatorTypes()
        daily = intervals.find { it.intervalInMinutes == CsiAggregationInterval.DAILY }
        weekly = intervals.find { it.intervalInMinutes == CsiAggregationInterval.WEEKLY }
        page = aggregators.find { it.name.equals(AggregatorType.PAGE) }
        shop = aggregators.find { it.name.equals(AggregatorType.SHOP) }

        createCsiAggregations()

        createUpdateEvents()

    }

    void createCsiAggregations() {

        CsiAggregation mvDailyPageCalculated = TestDataUtil.createCsiAggregation(irrelevant_CsiAggregationDate, daily, page, irrelevant_PageTag, irrelevant_Value, irrelevant_ResultIds, false)
        idDailyPageMvInitiallyOpenAndCalculated = mvDailyPageCalculated.ident()
        CsiAggregation mvDailyPageOutdated = TestDataUtil.createCsiAggregation(irrelevant_CsiAggregationDate, daily, page, irrelevant_PageTag, irrelevant_Value, irrelevant_ResultIds, false)
        idDailyPageMvInitiallyOpenAndOutdated = mvDailyPageOutdated.ident()

        CsiAggregation mvDailyShopCalculated = TestDataUtil.createCsiAggregation(irrelevant_CsiAggregationDate, daily, shop, irrelevant_ShopTag, irrelevant_Value, irrelevant_ResultIds, false)
        idDailyShopMvInitiallyOpenAndCalculated = mvDailyShopCalculated.ident()
        CsiAggregation mvDailyShopOutdated = TestDataUtil.createCsiAggregation(irrelevant_CsiAggregationDate, daily, shop, irrelevant_ShopTag, irrelevant_Value, irrelevant_ResultIds, false)
        idDailyShopMvInitiallyOpenAndOutdated = mvDailyShopOutdated.ident()

        CsiAggregation mvWeeklyPageCalculated = TestDataUtil.createCsiAggregation(irrelevant_CsiAggregationDate, weekly, page, irrelevant_PageTag, irrelevant_Value, irrelevant_ResultIds, false)
        idWeeklyPageMvInitiallyOpenAndCalculated = mvWeeklyPageCalculated.ident()
        CsiAggregation mvWeeklyPageOutdated = TestDataUtil.createCsiAggregation(irrelevant_CsiAggregationDate, weekly, page, irrelevant_PageTag, irrelevant_Value, irrelevant_ResultIds, false)
        idWeeklyPageMvInitiallyOpenAndOutdated = mvWeeklyPageOutdated.ident()

        CsiAggregation mvWeeklyShopCalculated = TestDataUtil.createCsiAggregation(irrelevant_CsiAggregationDate, weekly, shop, irrelevant_PageTag, irrelevant_Value, irrelevant_ResultIds, false)
        idWeeklyShopMvInitiallyOpenAndCalculated = mvWeeklyShopCalculated.ident()
        CsiAggregation mvWeeklyShopOutdated = TestDataUtil.createCsiAggregation(irrelevant_CsiAggregationDate, weekly, shop, irrelevant_PageTag, irrelevant_Value, irrelevant_ResultIds, false)
        idWeeklyShopMvInitiallyOpenAndOutdated = mvWeeklyShopOutdated.ident()
    }

    void createUpdateEvents() {
        TestDataUtil.createUpdateEvent(idDailyPageMvInitiallyOpenAndCalculated, CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        TestDataUtil.createUpdateEvent(idDailyPageMvInitiallyOpenAndOutdated, CsiAggregationUpdateEvent.UpdateCause.OUTDATED)

        TestDataUtil.createUpdateEvent(idDailyShopMvInitiallyOpenAndCalculated, CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        TestDataUtil.createUpdateEvent(idDailyShopMvInitiallyOpenAndOutdated, CsiAggregationUpdateEvent.UpdateCause.OUTDATED)

        TestDataUtil.createUpdateEvent(idWeeklyPageMvInitiallyOpenAndCalculated, CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        TestDataUtil.createUpdateEvent(idWeeklyPageMvInitiallyOpenAndOutdated, CsiAggregationUpdateEvent.UpdateCause.OUTDATED)

        TestDataUtil.createUpdateEvent(idWeeklyShopMvInitiallyOpenAndCalculated, CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        TestDataUtil.createUpdateEvent(idWeeklyShopMvInitiallyOpenAndOutdated, CsiAggregationUpdateEvent.UpdateCause.OUTDATED)
    }

    void addMocksCommonForAllTests() {

        ServiceMocker.create().mockBatchActivityService(serviceUnderTest)

        def inMemoryConfigService = new StubFor(InMemoryConfigService)
        inMemoryConfigService.demand.areMeasurementsGenerallyEnabled { ->
            return true
        }
        serviceUnderTest.inMemoryConfigService = inMemoryConfigService.proxyInstance()

        serviceUnderTest.csiAggregationTagService = new StubFor(CsiAggregationTagService).proxyInstance()

        def shopCsiAggregationService = new StubFor(ShopCsiAggregationService)
        shopCsiAggregationService.demand.calcCa { CsiAggregation toBeCalculated ->
            calculationCounts[toBeCalculated.ident()] = ++calculationCounts[toBeCalculated.ident()]
            return null
        }
        serviceUnderTest.shopCsiAggregationService = shopCsiAggregationService.proxyInstance()

        def pageCsiAggregationService = new StubFor(PageCsiAggregationService)
        pageCsiAggregationService.demand.getHmvsByCsiGroupPageCombinationMap { List<JobGroup> csiGroups, List<Page> csiPages, DateTime startDateTime, DateTime endDateTime ->
            Map irrelevantBecauseWholeCalculationIsMocked = [:]
            return irrelevantBecauseWholeCalculationIsMocked
        }
        pageCsiAggregationService.demand.calcMv { CsiAggregation toBeCalculated, CsiAggregationCachingContainer cachingContainer ->
            calculationCounts[toBeCalculated.ident()] = ++calculationCounts[toBeCalculated.ident()]
            return null

        }
        serviceUnderTest.pageCsiAggregationService = pageCsiAggregationService.proxyInstance()

        def csiAggregationTagService = new StubFor(CsiAggregationTagService)
        csiAggregationTagService.demand.getAllPagesFromWeeklyOrDailyPageTags { unused ->
            def result = [:].withDefault { [] }
            result[1].add(new Page())
            return result
        }
        csiAggregationTagService.demand.getAllJobGroupsFromWeeklyOrDailyPageTags { unused ->
            def result = [:].withDefault { [] }
            result[1].add(new JobGroup())
            return result
        }
        serviceUnderTest.csiAggregationTagService = csiAggregationTagService.proxyInstance()

        mockCachingContainerService()
    }

    void mockCachingContainerService() {
        ServiceMocker mocker = ServiceMocker.create()

        def returnForGetDailyHeCsiAggregationMapByStartDate = [:].withDefault { [:].withDefault { [] } }
        def returnForGetWeeklyHeCsiAggregationMapByStartDate = [:].withDefault { [:].withDefault { [] } }
        def returnForGetDailyJobGroupsByStartDate = [:].withDefault { [] }
        def returnForGetDailyPagesByStartDate = [:].withDefault { [] }
        def returnForGetWeeklyJobGroupsByStartDate = [:].withDefault { [] }
        def returnForGetWeeklyPagesByStartDate = [:].withDefault { [] }
        Page page = new Page(id: 1)
        def hCsiAggregationsByCsiGroupPageCombination = [:].withDefault { [] }
        hCsiAggregationsByCsiGroupPageCombination.put(CsiAggregation.get(idDailyShopMvInitiallyOpenAndOutdated).started.toString(), CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndOutdated))
        def returnForCreateContainerFor = new CsiAggregationCachingContainer(csiGroupToCalcCsiAggregationFor: null,
                pageToCalcCsiAggregationFor: page,
                hCsiAggregationsByCsiGroupPageCombination: hCsiAggregationsByCsiGroupPageCombination)

        mocker.mockCachingContainerService(serviceUnderTest, returnForGetDailyJobGroupsByStartDate, returnForGetDailyPagesByStartDate,
                returnForGetDailyHeCsiAggregationMapByStartDate, returnForGetWeeklyJobGroupsByStartDate, returnForGetWeeklyPagesByStartDate,
                returnForGetWeeklyHeCsiAggregationMapByStartDate, returnForCreateContainerFor)
    }
}
