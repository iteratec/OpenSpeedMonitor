package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import de.iteratec.osm.util.ServiceMocker
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback

/**
 * Created by nkuhn on 22.05.15.
 *
 * Has to be an integration test beacause detachedCriteria is not supported in unit tests
 */
@Integration
@Rollback
class CsiAggregationUpdateEventCleanupServiceSpec extends NonTransactionalIntegrationSpec {

    CsiAggregationUpdateEventCleanupService csiAggregationUpdateEventCleanupService

    CsiAggregationInterval daily
    CsiAggregationInterval weekly
    AggregatorType page
    AggregatorType shop

    def setup() {
        createTestDataCommonForAllTests()
        addMocksCommonForAllTests()
    }


    void "already calculated daily page mvs get closed"() {
        setup:
        CsiAggregation mvDailyPageCalculated = TestDataUtil.createSimpleCsiAggregation(new Date(100), daily, page, false)
        Serializable idDailyPageMvInitiallyOpenAndCalculated = mvDailyPageCalculated.ident()
        TestDataUtil.createUpdateEvent(idDailyPageMvInitiallyOpenAndCalculated, CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndCalculated).size() == 1
        assert CsiAggregation.get(idDailyPageMvInitiallyOpenAndCalculated).closedAndCalculated == false
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300, false)

        then:
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndCalculated).size() == 0
        CsiAggregation.get(idDailyPageMvInitiallyOpenAndCalculated).closedAndCalculated == true
    }


    void "outdated daily page mvs get calculated and closed"() {
        setup:
        CsiAggregation mvDailyPageOutdated = TestDataUtil.createSimpleCsiAggregation(new Date(100), daily, page, false)
        Serializable idDailyPageMvInitiallyOpenAndOutdated = mvDailyPageOutdated.ident()
        TestDataUtil.createUpdateEvent(idDailyPageMvInitiallyOpenAndOutdated, CsiAggregationUpdateEvent.UpdateCause.OUTDATED)

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndOutdated).size() == 1
        assert CsiAggregation.get(idDailyPageMvInitiallyOpenAndOutdated).closedAndCalculated == false
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndOutdated).size() == 0
        CsiAggregation.get(idDailyPageMvInitiallyOpenAndOutdated).closedAndCalculated == true
    }


    void "already calculated weekly page mvs get closed"() {
        setup:
        CsiAggregation mvWeeklyPageCalculated = TestDataUtil.createSimpleCsiAggregation(new Date(100), weekly, page, false)
        Serializable idWeeklyPageMvInitiallyOpenAndCalculated = mvWeeklyPageCalculated.ident()
        TestDataUtil.createUpdateEvent(idWeeklyPageMvInitiallyOpenAndCalculated, CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndCalculated).size() == 1
        assert CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndCalculated).closedAndCalculated == false
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndCalculated).size() == 0
        CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndCalculated).closedAndCalculated == true
    }


    void "outdated weekly page mvs get calculated and closed"() {
        setup:
        CsiAggregation mvWeeklyPageOutdated = TestDataUtil.createSimpleCsiAggregation(new Date(100), weekly, page, false)
        Serializable idWeeklyPageMvInitiallyOpenAndOutdated = mvWeeklyPageOutdated.ident()
        TestDataUtil.createUpdateEvent(idWeeklyPageMvInitiallyOpenAndOutdated, CsiAggregationUpdateEvent.UpdateCause.OUTDATED)

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndOutdated).size() == 1
        assert CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndOutdated).closedAndCalculated == false
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndOutdated).size() == 0
        CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndOutdated).closedAndCalculated == true
    }


    void "already calculated daily shop mvs get closed"() {
        setup:
        CsiAggregation mvDailyShopCalculated = TestDataUtil.createSimpleCsiAggregation(new Date(100), daily, shop, false)
        Serializable idDailyShopMvInitiallyOpenAndCalculated = mvDailyShopCalculated.ident()
        TestDataUtil.createUpdateEvent(idDailyShopMvInitiallyOpenAndCalculated, CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndCalculated).size() == 1
        assert CsiAggregation.get(idDailyShopMvInitiallyOpenAndCalculated).closedAndCalculated == false
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndCalculated).size() == 0
        CsiAggregation.get(idDailyShopMvInitiallyOpenAndCalculated).closedAndCalculated == true
    }


    void "outdated daily shop mvs get calculated and closed"() {
        setup:
        CsiAggregation mvDailyShopOutdated = TestDataUtil.createSimpleCsiAggregation(new Date(100), daily, shop, false)
        Serializable idDailyShopMvInitiallyOpenAndOutdated = mvDailyShopOutdated.ident()
        TestDataUtil.createUpdateEvent(idDailyShopMvInitiallyOpenAndOutdated, CsiAggregationUpdateEvent.UpdateCause.OUTDATED)

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndOutdated).size() == 1
        assert CsiAggregation.get(idDailyShopMvInitiallyOpenAndOutdated).closedAndCalculated == false
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndOutdated).size() == 0
        CsiAggregation.get(idDailyShopMvInitiallyOpenAndOutdated).closedAndCalculated == true
    }


    void "already calculated weekly shop mvs get closed"() {
        setup:
        CsiAggregation mvWeeklyShopCalculated = TestDataUtil.createSimpleCsiAggregation(new Date(100), weekly, shop, false)
        Serializable idWeeklyShopMvInitiallyOpenAndCalculated = mvWeeklyShopCalculated.ident()
        TestDataUtil.createUpdateEvent(idWeeklyShopMvInitiallyOpenAndCalculated, CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndCalculated).size() == 1
        assert CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndCalculated).closedAndCalculated == false
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndCalculated).size() == 0
        CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndCalculated).closedAndCalculated == true
    }


    void "outdated weekly shop mvs get calculated and closed"() {
        setup:
        CsiAggregation mvWeeklyShopOutdated = TestDataUtil.createSimpleCsiAggregation(new Date(100), weekly, shop, false)
        Serializable idWeeklyShopMvInitiallyOpenAndOutdated = mvWeeklyShopOutdated.ident()
        TestDataUtil.createUpdateEvent(idWeeklyShopMvInitiallyOpenAndOutdated, CsiAggregationUpdateEvent.UpdateCause.OUTDATED)

        when:
        assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndOutdated).size() == 1
        assert CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndOutdated).closedAndCalculated == false
        csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
        CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndOutdated).size() == 0
        CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndOutdated).closedAndCalculated == true
    }

    void createTestDataCommonForAllTests() {
        JobGroup.withNewTransaction {

            List<CsiAggregationInterval> intervals = TestDataUtil.createCsiAggregationIntervals()
            List<AggregatorType> aggregators = TestDataUtil.createAggregatorTypes()
            daily = intervals.find { it.intervalInMinutes == CsiAggregationInterval.DAILY }
            weekly = intervals.find { it.intervalInMinutes == CsiAggregationInterval.WEEKLY }
            page = aggregators.find { it.name.equals(AggregatorType.PAGE) }
            shop = aggregators.find { it.name.equals(AggregatorType.SHOP) }

            new Page(id: 1, name: "unused page", weight: 1.0).save(failOnError: true, flush: true)
            new JobGroup(id: 1, name: "unused JobGroup").save(failOnError: true, flush: true)
        }
    }

    void addMocksCommonForAllTests() {
        csiAggregationUpdateEventCleanupService.inMemoryConfigService.metaClass {
            areMeasurementsGenerallyEnabled { -> return true }
        }
        ServiceMocker.create().mockBatchActivityService(csiAggregationUpdateEventCleanupService)
    }
}
