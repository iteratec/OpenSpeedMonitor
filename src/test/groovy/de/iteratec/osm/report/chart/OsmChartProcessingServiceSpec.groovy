/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

package de.iteratec.osm.report.chart

import de.iteratec.osm.csi.CsiType
import de.iteratec.osm.util.I18nService
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import static de.iteratec.osm.util.Constants.HIGHCHART_LEGEND_DELIMITTER

/**
 * These tests test processing osm chart data.
 *
 * <b>Note: </b>Method {@link de.iteratec.osm.report.chart.OsmChartProcessingService#summarizeEventResultGraphs(java.util.List)}
 *      isn't tested here cause it is tested in class {@link de.iteratec.osm.result.SummarizedChartLegendEntriesSpec} already.
 */
class OsmChartProcessingServiceSpec extends Specification implements ServiceUnitTest<OsmChartProcessingService> {

    OsmChartProcessingService serviceUnderTest

    public static final String JOB_GROUP_1_NAME = 'group 1'
    public static final String JOB_GROUP_2_NAME = 'group 2'
    public static final String JOB_GROUP_3_NAME = 'group 3'
    public static final String JOB_GROUP_4_NAME = 'group 4'
    public static final String EVENT_1_NAME = 'event 1 name'
    public static final String EVENT_2_NAME = 'event 2 name'
    public static final String EVENT_3_NAME = 'event 3 name'
    public static final String EVENT_4_NAME = 'event 4 name'
    public static final String LOCATION_1_UNIQUE_IDENTIFIER = 'unique-identifier-location1'
    public static final String LOCATION_2_UNIQUE_IDENTIFIER = 'unique-identifier-location2'
    public static final String LOCATION_3_UNIQUE_IDENTIFIER = 'unique-identifier-location3'
    public static final String LOCATION_4_UNIQUE_IDENTIFIER = 'unique-identifier-location4'
    public static final String PAGE_1_NAME = 'pageAggregator 1 name'
    public static final String PAGE_2_NAME = 'pageAggregator 2 name'
    public static final String PAGE_3_NAME = 'pageAggregator 3 name'
    public static final String PAGE_4_NAME = 'pageAggregator 4 name'
    public static final String MEASURAND_1_NAME = 'measurand 1 name'
    public static final String MEASURAND_2_NAME = 'measurand 2 name'
    public static final String MEASURAND_3_NAME = 'measurand 3 name'
    public static final String MEASURAND_4_NAME = 'measurand 4 name'
    public static final String CONNECTIVITY_1_NAME = 'Connectivity 1'
    public static final String CONNECTIVITY_2_NAME = 'Connectivity 2'
    public static final String CONNECTIVITY_3_NAME = 'Connectivity 3'
    public static final String CONNECTIVITY_4_NAME = 'Connectivity 4'
    public static final String MEASURED_STEP_1_NAME = 'step 1'
    public static final String MEASURED_STEP_2_NAME = 'step 2'
    public static final String MEASURED_STEP_3_NAME = 'step 3'
    public static final String MEASURED_STEP_4_NAME = 'step 4'

    public static final String I18N_LABEL_JOB_GROUP = 'Job Group'
    public static final String I18N_LABEL_CSI_TYPE = 'Csi Type'
    public static final String I18N_LABEL_MEASURED_EVENT = 'Measured step'
    public static final String I18N_LABEL_LOCATION = 'Location'
    public static final String I18N_LABEL_MEASURAND = 'Measurand'
    public static final String I18N_LABEL_CONNECTIVITY = 'Connectivity'

    public void setup() {
        serviceUnderTest = service
        mocksCommonForAllTests()
    }

    // event csiAggregations ////////////////////////////////////////////////////////////////////////////

    void "event csiAggregations, every legend part in every graph different -> no summarization"() {
        setup:
        List<OsmChartGraph> graphs = [
                new OsmChartGraph(label: [JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [JOB_GROUP_2_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [JOB_GROUP_3_NAME, EVENT_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [JOB_GROUP_4_NAME, EVENT_4_NAME, LOCATION_4_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER))
        ]

        when:
        OsmRickshawChart chart = serviceUnderTest.summarizeCsiGraphs(graphs)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_2_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_3_NAME, EVENT_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_4_NAME, EVENT_4_NAME, LOCATION_4_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
        chart.osmChartGraphsCommonLabel == ''
    }

    void "event csiAggregations, some legend parts in every graph the same, some different -> summarization"() {
        setup:
        List<OsmChartGraph> graphs = [
                new OsmChartGraph(label: [CsiType.DOC_COMPLETE.toString(), JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [CsiType.DOC_COMPLETE.toString(), JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_2_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [CsiType.DOC_COMPLETE.toString(), JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_3_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [CsiType.DOC_COMPLETE.toString(), JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_4_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER))
        ]
        String expectedCommonLabel =
                "<b>${I18N_LABEL_CSI_TYPE}</b>: ${CsiType.DOC_COMPLETE.toString()} | " +
                        "<b>${I18N_LABEL_JOB_GROUP}</b>: ${JOB_GROUP_1_NAME} | " +
                        "<b>${I18N_LABEL_MEASURED_EVENT}</b>: ${EVENT_1_NAME}"

        when:
        OsmRickshawChart chart = serviceUnderTest.summarizeCsiGraphs(graphs)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                LOCATION_1_UNIQUE_IDENTIFIER,
                LOCATION_2_UNIQUE_IDENTIFIER,
                LOCATION_3_UNIQUE_IDENTIFIER,
                LOCATION_4_UNIQUE_IDENTIFIER
        ])
        chart.osmChartGraphsCommonLabel == expectedCommonLabel
    }

    void "event csiAggregations, single legend parts in some but not all graphs the same -> no summarization"() {
        setup:
        List<OsmChartGraph> graphs = [
                new OsmChartGraph(label: [JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [JOB_GROUP_1_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [JOB_GROUP_1_NAME, EVENT_3_NAME, LOCATION_2_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [JOB_GROUP_4_NAME, EVENT_4_NAME, LOCATION_4_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER))
        ]

        when:
        OsmRickshawChart chart = serviceUnderTest.summarizeCsiGraphs(graphs)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [JOB_GROUP_1_NAME, EVENT_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_1_NAME, EVENT_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_1_NAME, EVENT_3_NAME, LOCATION_2_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_4_NAME, EVENT_4_NAME, LOCATION_4_UNIQUE_IDENTIFIER].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
        chart.osmChartGraphsCommonLabel == ''
    }

    // pageAggregator csiAggregations ////////////////////////////////////////////////////////////////////////////

    void "page csiAggregations, every legend part in every graph different -> no summarization"() {
        setup:
        List<OsmChartGraph> graphs = [
                new OsmChartGraph(label: [JOB_GROUP_1_NAME, PAGE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [JOB_GROUP_2_NAME, PAGE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [JOB_GROUP_3_NAME, PAGE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [JOB_GROUP_4_NAME, PAGE_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
        ]

        when:
        OsmRickshawChart chart = serviceUnderTest.summarizeCsiGraphs(graphs)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [JOB_GROUP_1_NAME, PAGE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_2_NAME, PAGE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_3_NAME, PAGE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_4_NAME, PAGE_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
        chart.osmChartGraphsCommonLabel == ''
    }

    void "page csiAggregations, some legend parts in every graph the same, some different -> summarization"() {
        setup:
        List<OsmChartGraph> graphs = [
                new OsmChartGraph(label: [CsiType.DOC_COMPLETE.toString(), JOB_GROUP_1_NAME, PAGE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [CsiType.DOC_COMPLETE.toString(), JOB_GROUP_1_NAME, PAGE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [CsiType.VISUALLY_COMPLETE.toString(), JOB_GROUP_1_NAME, PAGE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [CsiType.VISUALLY_COMPLETE.toString(), JOB_GROUP_1_NAME, PAGE_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER))
        ]
        String expectedCommonLabel = "<b>${I18N_LABEL_JOB_GROUP}</b>: ${JOB_GROUP_1_NAME}"

        when:
        OsmRickshawChart chart = serviceUnderTest.summarizeCsiGraphs(graphs)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                CsiType.DOC_COMPLETE.toString() + HIGHCHART_LEGEND_DELIMITTER + PAGE_1_NAME,
                CsiType.DOC_COMPLETE.toString() + HIGHCHART_LEGEND_DELIMITTER + PAGE_2_NAME,
                CsiType.VISUALLY_COMPLETE.toString() + HIGHCHART_LEGEND_DELIMITTER + PAGE_3_NAME,
                CsiType.VISUALLY_COMPLETE.toString() + HIGHCHART_LEGEND_DELIMITTER + PAGE_4_NAME
        ])
        chart.osmChartGraphsCommonLabel == expectedCommonLabel
    }

    void "page csiAggregations, single legend parts in some but not all graphs the same -> no summarization"() {
        setup:
        List<OsmChartGraph> graphs = [
                new OsmChartGraph(label: [JOB_GROUP_1_NAME, PAGE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [JOB_GROUP_1_NAME, PAGE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [JOB_GROUP_1_NAME, PAGE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [JOB_GROUP_4_NAME, PAGE_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER))
        ]

        when:
        OsmRickshawChart chart = serviceUnderTest.summarizeCsiGraphs(graphs)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [JOB_GROUP_1_NAME, PAGE_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_1_NAME, PAGE_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_1_NAME, PAGE_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_4_NAME, PAGE_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
        chart.osmChartGraphsCommonLabel == ''
    }

    void "page csiAggregations, single legend parts begin with same chars but end different -> no summarization"() {
        given:
        List<OsmChartGraph> graphs = [
                new OsmChartGraph(label: ["jobGroup_Name", "HP"].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: ["jobGroup_Name", "HP_entry"].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: ["jobGroup_NameDifferent", "HP"].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: ["JobGroup_NameDifferent", "HP_entry"].join(HIGHCHART_LEGEND_DELIMITTER))
        ]

        when:
        OsmRickshawChart chart = serviceUnderTest.summarizeCsiGraphs(graphs)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                ["jobGroup_Name", "HP"].join(HIGHCHART_LEGEND_DELIMITTER),
                ["jobGroup_Name", "HP_entry"].join(HIGHCHART_LEGEND_DELIMITTER),
                ["jobGroup_NameDifferent", "HP"].join(HIGHCHART_LEGEND_DELIMITTER),
                ["JobGroup_NameDifferent", "HP_entry"].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
        chart.osmChartGraphsCommonLabel == ''
    }

    void "page csiAggregations, page name and jobGroup name are equal -> no summarization"() {
        given:
        List<OsmChartGraph> graphs = [
                new OsmChartGraph(label: ["HP", "HP"].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: ["HP", "HP_entry"].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: ["jobGroup_NameDifferent", "HP"].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: ["JobGroup_NameDifferent", "HP_entry"].join(HIGHCHART_LEGEND_DELIMITTER))
        ]

        when:
        OsmRickshawChart chart = serviceUnderTest.summarizeCsiGraphs(graphs)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                ["HP", "HP"].join(HIGHCHART_LEGEND_DELIMITTER),
                ["HP", "HP_entry"].join(HIGHCHART_LEGEND_DELIMITTER),
                ["jobGroup_NameDifferent", "HP"].join(HIGHCHART_LEGEND_DELIMITTER),
                ["JobGroup_NameDifferent", "HP_entry"].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
        chart.osmChartGraphsCommonLabel == ''
    }

//    ########## tests for summarizeEventResultGraphs #############

    void "event result, every legend part in every graph different -> no summarization"() {
        setup:
        List<OsmChartGraph> graphs = [
                new OsmChartGraph(label: [MEASURAND_1_NAME, JOB_GROUP_1_NAME, MEASURED_STEP_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, CONNECTIVITY_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [MEASURAND_2_NAME, JOB_GROUP_2_NAME, MEASURED_STEP_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, CONNECTIVITY_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [MEASURAND_3_NAME, JOB_GROUP_3_NAME, MEASURED_STEP_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, CONNECTIVITY_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [MEASURAND_4_NAME, JOB_GROUP_4_NAME, MEASURED_STEP_4_NAME, LOCATION_4_UNIQUE_IDENTIFIER, CONNECTIVITY_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
        ]

        when:
        OsmRickshawChart chart = serviceUnderTest.summarizeEventResultGraphs(graphs)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [MEASURAND_1_NAME, JOB_GROUP_1_NAME, MEASURED_STEP_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, CONNECTIVITY_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [MEASURAND_2_NAME, JOB_GROUP_2_NAME, MEASURED_STEP_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, CONNECTIVITY_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [MEASURAND_3_NAME, JOB_GROUP_3_NAME, MEASURED_STEP_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, CONNECTIVITY_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [MEASURAND_4_NAME, JOB_GROUP_4_NAME, MEASURED_STEP_4_NAME, LOCATION_4_UNIQUE_IDENTIFIER, CONNECTIVITY_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
        chart.osmChartGraphsCommonLabel == ''
    }

    void "event result, single legend part in some but not in all graphs same -> no summarization"() {
        setup:
        List<OsmChartGraph> graphs = [
                new OsmChartGraph(label: [MEASURAND_1_NAME, JOB_GROUP_1_NAME, MEASURED_STEP_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, CONNECTIVITY_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [MEASURAND_2_NAME, JOB_GROUP_2_NAME, MEASURED_STEP_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, CONNECTIVITY_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [MEASURAND_2_NAME, JOB_GROUP_3_NAME, MEASURED_STEP_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, CONNECTIVITY_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [MEASURAND_4_NAME, JOB_GROUP_4_NAME, MEASURED_STEP_3_NAME, LOCATION_4_UNIQUE_IDENTIFIER, CONNECTIVITY_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
        ]

        when:
        OsmRickshawChart chart = serviceUnderTest.summarizeEventResultGraphs(graphs)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [MEASURAND_1_NAME, JOB_GROUP_1_NAME, MEASURED_STEP_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, CONNECTIVITY_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [MEASURAND_2_NAME, JOB_GROUP_2_NAME, MEASURED_STEP_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, CONNECTIVITY_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [MEASURAND_2_NAME, JOB_GROUP_3_NAME, MEASURED_STEP_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, CONNECTIVITY_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [MEASURAND_4_NAME, JOB_GROUP_4_NAME, MEASURED_STEP_3_NAME, LOCATION_4_UNIQUE_IDENTIFIER, CONNECTIVITY_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
        chart.osmChartGraphsCommonLabel == ''
    }

    void "event result, some legend parts in every graph the same, some different -> summarization"() {
        setup:
        List<OsmChartGraph> graphs = [
                new OsmChartGraph(label: [MEASURAND_1_NAME, JOB_GROUP_1_NAME, MEASURED_STEP_1_NAME, LOCATION_4_UNIQUE_IDENTIFIER, CONNECTIVITY_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [MEASURAND_1_NAME, JOB_GROUP_2_NAME, MEASURED_STEP_2_NAME, LOCATION_4_UNIQUE_IDENTIFIER, CONNECTIVITY_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [MEASURAND_1_NAME, JOB_GROUP_3_NAME, MEASURED_STEP_3_NAME, LOCATION_4_UNIQUE_IDENTIFIER, CONNECTIVITY_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [MEASURAND_1_NAME, JOB_GROUP_4_NAME, MEASURED_STEP_3_NAME, LOCATION_4_UNIQUE_IDENTIFIER, CONNECTIVITY_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
        ]

        when:
        OsmRickshawChart chart = serviceUnderTest.summarizeEventResultGraphs(graphs)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [JOB_GROUP_1_NAME, MEASURED_STEP_1_NAME, CONNECTIVITY_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_2_NAME, MEASURED_STEP_2_NAME, CONNECTIVITY_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_3_NAME, MEASURED_STEP_3_NAME, CONNECTIVITY_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [JOB_GROUP_4_NAME, MEASURED_STEP_3_NAME, CONNECTIVITY_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
        chart.osmChartGraphsCommonLabel == "<b>${I18N_LABEL_MEASURAND}</b>: ${MEASURAND_1_NAME} | " +
                "<b>${I18N_LABEL_LOCATION}</b>: ${LOCATION_4_UNIQUE_IDENTIFIER}"

    }

    void "event result, single legend parts begin with same chars but end different -> no summarization"() {
        setup:
        List<OsmChartGraph> graphs = [
                new OsmChartGraph(label: [MEASURAND_1_NAME, "JobGroup_1_Name", MEASURED_STEP_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, CONNECTIVITY_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [MEASURAND_2_NAME, "JobGroup_1_NameDifferent", MEASURED_STEP_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, CONNECTIVITY_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [MEASURAND_2_NAME, "JobGroup_1_Name", MEASURED_STEP_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, CONNECTIVITY_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
                new OsmChartGraph(label: [MEASURAND_4_NAME, "JobGroup_1_NameDifferent", MEASURED_STEP_3_NAME, LOCATION_4_UNIQUE_IDENTIFIER, CONNECTIVITY_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER)),
        ]

        when:
        OsmRickshawChart chart = serviceUnderTest.summarizeEventResultGraphs(graphs)
        List<OsmChartGraph> resultGraphs = chart.osmChartGraphs

        then:
        resultGraphs.size() == 4
        List<String> graphLables = resultGraphs*.label
        graphLables.containsAll([
                [MEASURAND_1_NAME, "JobGroup_1_Name", MEASURED_STEP_1_NAME, LOCATION_1_UNIQUE_IDENTIFIER, CONNECTIVITY_1_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [MEASURAND_2_NAME, "JobGroup_1_NameDifferent", MEASURED_STEP_2_NAME, LOCATION_2_UNIQUE_IDENTIFIER, CONNECTIVITY_2_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [MEASURAND_2_NAME, "JobGroup_1_Name", MEASURED_STEP_3_NAME, LOCATION_3_UNIQUE_IDENTIFIER, CONNECTIVITY_3_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
                [MEASURAND_4_NAME, "JobGroup_1_NameDifferent", MEASURED_STEP_3_NAME, LOCATION_4_UNIQUE_IDENTIFIER, CONNECTIVITY_4_NAME].join(HIGHCHART_LEGEND_DELIMITTER),
        ])
        chart.osmChartGraphsCommonLabel == ''
    }

//    ######### end of test for summarizeEventResultGraphs ########


    void mocksCommonForAllTests() {
        serviceUnderTest.i18nService = Stub(I18nService)
        serviceUnderTest.i18nService.msg(_, _) >> { String msgKey, String defaultMessage = null, List objs = null ->
            Map i18nKeysToValues = [
                    'job.jobGroup.label'                         : I18N_LABEL_JOB_GROUP,
                    'de.iteratec.osm.result.measured-event.label': I18N_LABEL_MEASURED_EVENT,
                    'job.location.label'                         : I18N_LABEL_LOCATION,
                    'de.iteratec.result.measurand.label'         : I18N_LABEL_MEASURAND,
                    'de.iteratec.osm.result.connectivity.label'  : I18N_LABEL_CONNECTIVITY,
                    'de.iteratec.osm.csi.type.heading'           : I18N_LABEL_CSI_TYPE
            ]
            return i18nKeysToValues[msgKey]
        }
    }
}
