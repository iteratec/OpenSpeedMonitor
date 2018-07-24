package de.iteratec.osm.csi

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

/**
 * Created by nkuhn on 22.05.15.
 *
 * Has to be an integration test because detachedCriteria is not supported in unit tests
 */
@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class CsiAggregationUpdateEventCleanupServiceIntegrationSpec extends NonTransactionalIntegrationSpec {

    CsiAggregationUpdateEventCleanupService csiAggregationUpdateEventCleanupService

    CsiAggregationInterval daily
    CsiAggregationInterval weekly

    def setup() {
        createTestDataCommonForAllTests()
        addMocksCommonForAllTests()
    }

    def cleanup() {
        csiAggregationUpdateEventCleanupService.inMemoryConfigService = grailsApplication.mainContext.getBean('inMemoryConfigService')
    }

    void "already calculated daily page csi aggregations get closed"() {
        setup:
        long idDailyPageMvInitiallyOpenAndCalculated
            CsiAggregation csiAggregationDailyPageCalculated = CsiAggregation.build(started: new Date(100), interval: daily, aggregationType: AggregationType.PAGE, closedAndCalculated: false)
            idDailyPageMvInitiallyOpenAndCalculated = csiAggregationDailyPageCalculated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idDailyPageMvInitiallyOpenAndCalculated, updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        when:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndCalculated).size() == 1
            assert !CsiAggregation.get(idDailyPageMvInitiallyOpenAndCalculated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndCalculated).size() == 0
            CsiAggregation.get(idDailyPageMvInitiallyOpenAndCalculated).closedAndCalculated
        }

    void "outdated daily page csi aggregations get calculated and closed"() {
        setup:
        long idDailyPageMvInitiallyOpenAndOutdated
            CsiAggregation mvDailyPageOutdated = CsiAggregation.build(started: new Date(100), interval: daily, aggregationType: AggregationType.PAGE, closedAndCalculated: false)
            idDailyPageMvInitiallyOpenAndOutdated = mvDailyPageOutdated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idDailyPageMvInitiallyOpenAndOutdated, updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED)

        when:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndOutdated).size() == 1
            assert !CsiAggregation.get(idDailyPageMvInitiallyOpenAndOutdated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndOutdated).size() == 0
            CsiAggregation.get(idDailyPageMvInitiallyOpenAndOutdated).closedAndCalculated
        }

    void "already calculated weekly page csi aggregations get closed"() {
        setup:
        long idWeeklyPageMvInitiallyOpenAndCalculated
            CsiAggregation mvWeeklyPageCalculated = CsiAggregation.build(started: new Date(100), interval: weekly, aggregationType: AggregationType.PAGE, closedAndCalculated: false)
            idWeeklyPageMvInitiallyOpenAndCalculated = mvWeeklyPageCalculated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idWeeklyPageMvInitiallyOpenAndCalculated, updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        when:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndCalculated).size() == 1
            assert !CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndCalculated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndCalculated).size() == 0
            CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndCalculated).closedAndCalculated
        }

    void "outdated weekly page csi aggregations get calculated and closed"() {
        setup:
        long idWeeklyPageMvInitiallyOpenAndOutdated
            CsiAggregation mvWeeklyPageOutdated = CsiAggregation.build(started: new Date(100), interval: weekly, aggregationType: AggregationType.PAGE, closedAndCalculated: false)
            idWeeklyPageMvInitiallyOpenAndOutdated = mvWeeklyPageOutdated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idWeeklyPageMvInitiallyOpenAndOutdated, updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED)

        when:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndOutdated).size() == 1
            assert !CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndOutdated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndOutdated).size() == 0
            CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndOutdated).closedAndCalculated
        }

    void "already calculated daily shop csi aggregations get closed"() {
        setup:
        long idDailyShopMvInitiallyOpenAndCalculated
            CsiAggregation mvDailyShopCalculated = CsiAggregation.build(started: new Date(100), interval: daily, aggregationType: AggregationType.JOB_GROUP, closedAndCalculated: false)
            idDailyShopMvInitiallyOpenAndCalculated = mvDailyShopCalculated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idDailyShopMvInitiallyOpenAndCalculated, updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        when:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndCalculated).size() == 1
            assert !CsiAggregation.get(idDailyShopMvInitiallyOpenAndCalculated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndCalculated).size() == 0
            CsiAggregation.get(idDailyShopMvInitiallyOpenAndCalculated).closedAndCalculated
        }

    void "outdated daily shop csi aggregations get calculated and closed"() {
        setup:
        long idDailyShopMvInitiallyOpenAndOutdated
            CsiAggregation mvDailyShopOutdated = CsiAggregation.build(started: new Date(100), interval: daily, aggregationType: AggregationType.JOB_GROUP, closedAndCalculated: false)
            idDailyShopMvInitiallyOpenAndOutdated = mvDailyShopOutdated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idDailyShopMvInitiallyOpenAndOutdated, updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED)

        when:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndOutdated).size() == 1
            assert !CsiAggregation.get(idDailyShopMvInitiallyOpenAndOutdated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndOutdated).size() == 0
            CsiAggregation.get(idDailyShopMvInitiallyOpenAndOutdated).closedAndCalculated
        }

    void "already calculated weekly shop csi aggregations get closed"() {
        setup:
        long idWeeklyShopMvInitiallyOpenAndCalculated
            CsiAggregation mvWeeklyShopCalculated = CsiAggregation.build(started: new Date(100), interval: weekly, aggregationType: AggregationType.JOB_GROUP, closedAndCalculated: false)
            idWeeklyShopMvInitiallyOpenAndCalculated = mvWeeklyShopCalculated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idWeeklyShopMvInitiallyOpenAndCalculated, updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED)

        when:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndCalculated).size() == 1
            assert !CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndCalculated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndCalculated).size() == 0
            CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndCalculated).closedAndCalculated
        }

    void "outdated weekly shop csi aggregations get calculated and closed"() {
        setup:
        long idWeeklyShopMvInitiallyOpenAndOutdated
            CsiAggregation mvWeeklyShopOutdated = CsiAggregation.build(started: new Date(100), interval: weekly, aggregationType: AggregationType.JOB_GROUP, closedAndCalculated: false)
            idWeeklyShopMvInitiallyOpenAndOutdated = mvWeeklyShopOutdated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idWeeklyShopMvInitiallyOpenAndOutdated, updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED)

        when:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndOutdated).size() == 1
            assert !CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndOutdated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)

        then:
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndOutdated).size() == 0
            CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndOutdated).closedAndCalculated
        }

    void createTestDataCommonForAllTests() {
        daily = CsiAggregationInterval.build(name: "daily", intervalInMinutes: CsiAggregationInterval.DAILY, save: false)
        weekly = CsiAggregationInterval.build(name: "weekly", intervalInMinutes: CsiAggregationInterval.WEEKLY, save: false)
    }

    void addMocksCommonForAllTests() {
        def stub = Stub(InMemoryConfigService)
        stub.areMeasurementsGenerallyEnabled() >> { true }
        csiAggregationUpdateEventCleanupService.inMemoryConfigService = stub
    }

}
