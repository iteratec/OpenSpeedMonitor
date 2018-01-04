package de.iteratec.osm.result.dao

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.dao.CriteriaSorting
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.result.*


@Integration
@Rollback
class QueryEventResultsByConnectivitySpec extends NonTransactionalIntegrationSpec {

    EventResultDaoService eventResultDaoService

    public static final String CUSTOM_CONNECTIVITY_NAME_1 = 'Custom (6000/512 Kbps, 100ms, 0% PLR)'
    public static final String CUSTOM_CONNECTIVITY_NAME_2 = 'Custom (50000/6000 Kbps, 100ms, 0% PLR)'

    private DateTime runDate = new DateTime(2013, 5, 29, 0, 0, 0, DateTimeZone.UTC)

    private ConnectivityProfile predefinedConnectivityProfile1
    private ConnectivityProfile predefinedConnectivityProfile2

    private EventResult eventResultWithPredefinedProfile1
    private EventResult eventResultWithPredefinedProfile2
    private EventResult eventResultWithCustomConnectivity1
    private EventResult eventResultWithCustomConnectivity2
    private EventResult eventResultWithNativeConnectivity


    def setupForFeatureMethod() {
        EventResult.withNewSession { session ->
            createTestDataCommonToAllTests()
            session.flush()
        }
    }

    def "select event results by single predefined connectivity profile"() {
        setup: "database with a variety of event results, each with a different connectivity specification"
        setupForFeatureMethod()
        Collection<EventResult> results = null

        when: "a query for one single predefined connectivity profile is executed"
        EventResult.withNewSession {

            MvQueryParams queryParams = new ErQueryParams()
            queryParams.connectivityProfileIds.add(predefinedConnectivityProfile1.id)

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [CachedView.CACHED, CachedView.UNCACHED] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then: "the only event result with this connectivity profile is found"
        results.size() == 1
        results[0].connectivityProfile.id == predefinedConnectivityProfile1.id
        results[0].customConnectivityName == null
    }

    def "select event results by a list of predefined connectivity profiles"() {
        setup: "database with a variety of event results, each with a different connectivity specification"
        setupForFeatureMethod()
        Collection<EventResult> results = null

        when: "a query for two connectivity profiles is executed"
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams()
            queryParams.connectivityProfileIds.addAll([
                    predefinedConnectivityProfile1.id,
                    predefinedConnectivityProfile2.id
            ])

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [CachedView.CACHED, CachedView.UNCACHED] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then: "both event results with the corresponding connectivity profile is found"
        results.size() == 2
        List connectivityProfileIdsOfQueriedEventResults = results*.connectivityProfile*.id
        connectivityProfileIdsOfQueriedEventResults.contains(predefinedConnectivityProfile1.id)
        connectivityProfileIdsOfQueriedEventResults.contains(predefinedConnectivityProfile2.id)
        results[0].customConnectivityName == null
        results[1].customConnectivityName == null
    }

    def "select event results by one custom connectivity name"() {
        setup: "database with a variety of event results, each with a different connectivity specification"
        setupForFeatureMethod()
        Collection<EventResult> results

        when: "a query for one custom connectivity name is executed"
        MvQueryParams queryParams = new ErQueryParams()
        queryParams.customConnectivityNames.add(CUSTOM_CONNECTIVITY_NAME_1)

        results = eventResultDaoService.getLimitedMedianEventResultsBy(
                runDate.toDate(),
                runDate.plusHours(1).toDate(),
                [CachedView.CACHED, CachedView.UNCACHED] as Set,
                queryParams,
                [:],
                new CriteriaSorting(sortingActive: false)
        )

        then: "the only event result with this custom connectivity is found"
        results.size() == 1
        results[0].connectivityProfile == null
        results[0].customConnectivityName == CUSTOM_CONNECTIVITY_NAME_1
    }

    def "select event results by all present custom connectivity names"() {
        setup: "database with a variety of event results, each with a different connectivity specification"
        setupForFeatureMethod()
        Collection<EventResult> results = null

        when: "a query with all custom connectivity names is executed"
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams()
            queryParams.customConnectivityNames.addAll([
                    CUSTOM_CONNECTIVITY_NAME_1,
                    CUSTOM_CONNECTIVITY_NAME_2
            ])

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [CachedView.CACHED, CachedView.UNCACHED] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then: "both event results (one for each custom connectivity) are found"
        results.size() == 2
        List predefinedProfilesOfQueriedEventResults = results*.connectivityProfile
        predefinedProfilesOfQueriedEventResults[0] == null
        predefinedProfilesOfQueriedEventResults[1] == null
        results*.customConnectivityName.contains(eventResultWithCustomConnectivity1.customConnectivityName)
        results*.customConnectivityName.contains(eventResultWithCustomConnectivity2.customConnectivityName)
    }

    def "select only event results with native connectivity"() {
        setup: "database with a variety of event results, each with a different connectivity specification"
        setupForFeatureMethod()
        Collection<EventResult> results = null

        when: "a query for event results with native connectivity is executed"
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams()
            queryParams.includeNativeConnectivity = true

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [CachedView.CACHED, CachedView.UNCACHED] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then: "the only event result with native connectivity is found"
        results.size() == 1
        results[0].connectivityProfile == null
    }

    def "select event results by custom connectivity name regex AND native connectivity"() {
        setup: "database with a variety of event results, each with a different connectivity specification"
        setupForFeatureMethod()
        Collection<EventResult> results = null

        when: "a query for all event results with one specific custom connectivity name or native connectivity is executed"
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams()
            queryParams.customConnectivityNames.add(CUSTOM_CONNECTIVITY_NAME_1)
            queryParams.includeNativeConnectivity = true

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [CachedView.CACHED, CachedView.UNCACHED] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then: "both event results, one for custom and one for native connectivity are found"
        results.size() == 2
        results.findAll { it.connectivityProfile }.size() == 0
        results.findAll { it.customConnectivityName == CUSTOM_CONNECTIVITY_NAME_1 }.size() == 1
    }

    def "select event results by custom connectivity name regex AND predefined connectivity"() {
        setup: "database with a variety of event results, each with a different connectivity specification"
        setupForFeatureMethod()
        Collection<EventResult> results = null

        when: "a query for all custom connectivity names or a specific predefined connectivity profile is executed"
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams()
            queryParams.customConnectivityNames.addAll([
                    CUSTOM_CONNECTIVITY_NAME_1,
                    CUSTOM_CONNECTIVITY_NAME_2
            ])
            queryParams.connectivityProfileIds.add(predefinedConnectivityProfile1.id)

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [CachedView.CACHED, CachedView.UNCACHED] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then: "all three event results, one for each custom connectivity and one with a predefined connectivity profile are found"
        results.size() == 3
        results.findAll { it.connectivityProfile }.size() == 1
        results.findAll { it.customConnectivityName == CUSTOM_CONNECTIVITY_NAME_1 }.size() == 1
        results.findAll { it.customConnectivityName == CUSTOM_CONNECTIVITY_NAME_2 }.size() == 1
    }

    def "select event results by native connectivity AND predefined connectivity"() {
        setup: "database with a variety of event results, each with a different connectivity specification"
        setupForFeatureMethod()
        Collection<EventResult> results = null

        when: "a query for event results with native connectivity and one predefined connectivity is executed"
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams()
            queryParams.connectivityProfileIds.add(predefinedConnectivityProfile2.id)
            queryParams.includeNativeConnectivity = true

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [CachedView.CACHED, CachedView.UNCACHED] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then: "then two event results, one for each case are found"
        results.size() == 2
        results.findAll { it.connectivityProfile }.size() == 1
        results.findAll { it.noTrafficShapingAtAll }.size() == 1
    }

    def "select event results by custom connectivity name regex AND native connectivity AND predefined connectivity"() {
        setup: "database with a variety of event results, each with a different connectivity specification"
        setupForFeatureMethod()
        Collection<EventResult> results = null

        when: "a query for one custom, one predefined and native connectivity is executed"
        EventResult.withNewSession {
            MvQueryParams queryParams = new ErQueryParams()
            queryParams.connectivityProfileIds.add(predefinedConnectivityProfile2.id)
            queryParams.customConnectivityNames.add(CUSTOM_CONNECTIVITY_NAME_1)
            queryParams.includeNativeConnectivity = true

            results = eventResultDaoService.getLimitedMedianEventResultsBy(
                    runDate.toDate(),
                    runDate.plusHours(1).toDate(),
                    [CachedView.CACHED, CachedView.UNCACHED] as Set,
                    queryParams,
                    [:],
                    new CriteriaSorting(sortingActive: false)
            )
        }

        then: "three event results, one for each case are found"
        results.size() == 3
        results.findAll { it.connectivityProfile }.size() == 1
        results.findAll { it.connectivityProfile }.first().connectivityProfile.id == predefinedConnectivityProfile2.id
        results.findAll { it.customConnectivityName == CUSTOM_CONNECTIVITY_NAME_1 }.size() == 1
        results.findAll { it.noTrafficShapingAtAll }.size() == 1
    }


    private void createTestDataCommonToAllTests() {
        predefinedConnectivityProfile1 = ConnectivityProfile.build(name: 'predefined connectivity profile 1')
        eventResultWithPredefinedProfile1 = EventResult.build(
                connectivityProfile: predefinedConnectivityProfile1,
                jobResultDate: runDate.toDate(),
                medianValue: true,
        )

        predefinedConnectivityProfile2 = ConnectivityProfile.build(name: 'predefined connectivity profile 2')
        eventResultWithPredefinedProfile2 = EventResult.build(
                connectivityProfile: predefinedConnectivityProfile2,
                jobResultDate: runDate.toDate(),
                medianValue: true,
        )
        eventResultWithCustomConnectivity1 = EventResult.build(
                customConnectivityName: CUSTOM_CONNECTIVITY_NAME_1,
                connectivityProfile: null,
                noTrafficShapingAtAll: false,
                jobResultDate: runDate.toDate(),
                medianValue: true,
        )
        eventResultWithCustomConnectivity2 = EventResult.build(
                customConnectivityName: CUSTOM_CONNECTIVITY_NAME_2,
                connectivityProfile: null,
                noTrafficShapingAtAll: false,
                jobResultDate: runDate.toDate(),
                medianValue: true,
        )
        eventResultWithNativeConnectivity = EventResult.build(
                connectivityProfile: null,
                customConnectivityName: null,
                noTrafficShapingAtAll: true,
                jobResultDate: runDate.toDate(),
                medianValue: true
        )
    }
}
