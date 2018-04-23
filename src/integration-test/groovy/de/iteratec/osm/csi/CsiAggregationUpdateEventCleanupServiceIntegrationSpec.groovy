package de.iteratec.osm.csi

import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.AggregationType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.CsiAggregationInterval
import de.iteratec.osm.report.chart.CsiAggregationUpdateEvent
import grails.testing.mixin.integration.Integration
import org.springframework.test.annotation.Rollback

/**
 * Created by nkuhn on 22.05.15.
 *
 * Has to be an integration test because detachedCriteria is not supported in unit tests
 */
@Integration
@Rollback
class CsiAggregationUpdateEventCleanupServiceIntegrationSpec extends NonTransactionalIntegrationSpec {

    CsiAggregationUpdateEventCleanupService csiAggregationUpdateEventCleanupService

    CsiAggregationInterval daily
    CsiAggregationInterval weekly

    def setup() {
        createTestDataCommonForAllTests()
        addMocksCommonForAllTests()
    }


    void "already calculated daily page csi aggregations get closed"() {
        setup:
        long idDailyPageMvInitiallyOpenAndCalculated
        CsiAggregation.withNewSession {
            CsiAggregation csiAggregationDailyPageCalculated = CsiAggregation.build(started: new Date(100), interval: daily, aggregationType: AggregationType.PAGE, closedAndCalculated: false)
            idDailyPageMvInitiallyOpenAndCalculated = csiAggregationDailyPageCalculated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idDailyPageMvInitiallyOpenAndCalculated, updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        }

        when:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndCalculated).size() == 1
            assert !CsiAggregation.get(idDailyPageMvInitiallyOpenAndCalculated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }

        then:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndCalculated).size() == 0
            CsiAggregation.get(idDailyPageMvInitiallyOpenAndCalculated).closedAndCalculated
        }
    }


    void "outdated daily page csi aggregations get calculated and closed"() {
        setup:
        long idDailyPageMvInitiallyOpenAndOutdated
        CsiAggregation.withNewSession {
            CsiAggregation mvDailyPageOutdated = CsiAggregation.build(started: new Date(100), interval: daily, aggregationType: AggregationType.PAGE, closedAndCalculated: false)
            idDailyPageMvInitiallyOpenAndOutdated = mvDailyPageOutdated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idDailyPageMvInitiallyOpenAndOutdated, updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED)
        }

        when:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndOutdated).size() == 1
            assert !CsiAggregation.get(idDailyPageMvInitiallyOpenAndOutdated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }

        then:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyPageMvInitiallyOpenAndOutdated).size() == 0
            CsiAggregation.get(idDailyPageMvInitiallyOpenAndOutdated).closedAndCalculated
        }
    }


    void "already calculated weekly page csi aggregations get closed"() {
        setup:
        long idWeeklyPageMvInitiallyOpenAndCalculated
        CsiAggregation.withNewSession {
            CsiAggregation mvWeeklyPageCalculated = CsiAggregation.build(started: new Date(100), interval: weekly, aggregationType: AggregationType.PAGE, closedAndCalculated: false)
            idWeeklyPageMvInitiallyOpenAndCalculated = mvWeeklyPageCalculated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idWeeklyPageMvInitiallyOpenAndCalculated, updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        }

        when:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndCalculated).size() == 1
            assert !CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndCalculated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }

        then:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndCalculated).size() == 0
            CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndCalculated).closedAndCalculated
        }
    }


    void "outdated weekly page csi aggregations get calculated and closed"() {
        setup:
        long idWeeklyPageMvInitiallyOpenAndOutdated
        CsiAggregation.withNewSession {
            CsiAggregation mvWeeklyPageOutdated = CsiAggregation.build(started: new Date(100), interval: weekly, aggregationType: AggregationType.PAGE, closedAndCalculated: false)
            idWeeklyPageMvInitiallyOpenAndOutdated = mvWeeklyPageOutdated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idWeeklyPageMvInitiallyOpenAndOutdated, updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED)
        }

        when:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndOutdated).size() == 1
            assert !CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndOutdated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }

        then:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyPageMvInitiallyOpenAndOutdated).size() == 0
            CsiAggregation.get(idWeeklyPageMvInitiallyOpenAndOutdated).closedAndCalculated
        }
    }


    void "already calculated daily shop csi aggregations get closed"() {
        setup:
        long idDailyShopMvInitiallyOpenAndCalculated
        CsiAggregation.withNewSession {
            CsiAggregation mvDailyShopCalculated = CsiAggregation.build(started: new Date(100), interval: daily, aggregationType: AggregationType.JOB_GROUP, closedAndCalculated: false)
            idDailyShopMvInitiallyOpenAndCalculated = mvDailyShopCalculated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idDailyShopMvInitiallyOpenAndCalculated, updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        }

        when:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndCalculated).size() == 1
            assert !CsiAggregation.get(idDailyShopMvInitiallyOpenAndCalculated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }

        then:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndCalculated).size() == 0
            CsiAggregation.get(idDailyShopMvInitiallyOpenAndCalculated).closedAndCalculated
        }
    }


    void "outdated daily shop csi aggregations get calculated and closed"() {
        setup:
        long idDailyShopMvInitiallyOpenAndOutdated
        CsiAggregation.withNewSession {
            CsiAggregation mvDailyShopOutdated = CsiAggregation.build(started: new Date(100), interval: daily, aggregationType: AggregationType.JOB_GROUP, closedAndCalculated: false)
            idDailyShopMvInitiallyOpenAndOutdated = mvDailyShopOutdated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idDailyShopMvInitiallyOpenAndOutdated, updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED)
        }

        when:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndOutdated).size() == 1
            assert !CsiAggregation.get(idDailyShopMvInitiallyOpenAndOutdated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }

        then:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idDailyShopMvInitiallyOpenAndOutdated).size() == 0
            CsiAggregation.get(idDailyShopMvInitiallyOpenAndOutdated).closedAndCalculated
        }
    }


    void "already calculated weekly shop csi aggregations get closed"() {
        setup:
        long idWeeklyShopMvInitiallyOpenAndCalculated
        CsiAggregation.withNewSession {
            CsiAggregation mvWeeklyShopCalculated = CsiAggregation.build(started: new Date(100), interval: weekly, aggregationType: AggregationType.JOB_GROUP, closedAndCalculated: false)
            idWeeklyShopMvInitiallyOpenAndCalculated = mvWeeklyShopCalculated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idWeeklyShopMvInitiallyOpenAndCalculated, updateCause: CsiAggregationUpdateEvent.UpdateCause.CALCULATED)
        }

        when:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndCalculated).size() == 1
            assert !CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndCalculated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }

        then:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndCalculated).size() == 0
            CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndCalculated).closedAndCalculated
        }
    }


    void "outdated weekly shop csi aggregations get calculated and closed"() {
        setup:
        long idWeeklyShopMvInitiallyOpenAndOutdated
        CsiAggregation.withNewSession {
            CsiAggregation mvWeeklyShopOutdated = CsiAggregation.build(started: new Date(100), interval: weekly, aggregationType: AggregationType.JOB_GROUP, closedAndCalculated: false)
            idWeeklyShopMvInitiallyOpenAndOutdated = mvWeeklyShopOutdated.id
            CsiAggregationUpdateEvent.build(csiAggregationId: idWeeklyShopMvInitiallyOpenAndOutdated, updateCause: CsiAggregationUpdateEvent.UpdateCause.OUTDATED)
        }

        when:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndOutdated).size() == 1
            assert !CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndOutdated).closedAndCalculated
            csiAggregationUpdateEventCleanupService.closeCsiAggregationsExpiredForAtLeast(300)
        }

        then:
        CsiAggregation.withNewSession {
            assert CsiAggregationUpdateEvent.findAllByCsiAggregationId(idWeeklyShopMvInitiallyOpenAndOutdated).size() == 0
            CsiAggregation.get(idWeeklyShopMvInitiallyOpenAndOutdated).closedAndCalculated
        }
    }

    void createTestDataCommonForAllTests() {
        JobGroup.withNewTransaction {
            daily = CsiAggregationInterval.build(name: "daily", intervalInMinutes: CsiAggregationInterval.DAILY)
            weekly = CsiAggregationInterval.build(name: "weekly", intervalInMinutes: CsiAggregationInterval.WEEKLY)
        }
    }

    void addMocksCommonForAllTests() {
        def stub = Stub(InMemoryConfigService)
        stub.areMeasurementsGenerallyEnabled() >> { true }
        csiAggregationUpdateEventCleanupService.inMemoryConfigService = stub
    }

}
