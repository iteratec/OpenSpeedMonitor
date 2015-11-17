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

import de.iteratec.osm.util.I18nService

import static de.iteratec.osm.util.Constants.HIGHCHART_LEGEND_DELIMITTER

/**
 * OsmChartProcessingService
 * A service class encapsulates the core business logic of a Grails application
 */
class OsmChartProcessingService {

    static transactional = false

    I18nService i18nService

    OsmRickshawChart summarizeCsiGraphs(List<OsmChartGraph> graphs) {

        String commonLabel = ''

        if (graphs.size > 1) {
            def labelPartsToSummarize = [];

            // Split all Labels in individual parts
            List<List<String>> allTokenizedLabels = graphs*.label*.tokenize(HIGHCHART_LEGEND_DELIMITTER.trim())
            int labelPartCount = allTokenizedLabels[0].size()

            final List<String> keys = [i18nService.msg('job.jobGroup.label', 'Job Group'),
                                       i18nService.msg('de.iteratec.osm.result.measured-event.label', 'Measured step'),
                                       i18nService.msg('job.location.label', 'Location')]


            labelPartCount.times { i ->
                String keyToCheck = allTokenizedLabels[0][i]

                // check if all label parts[i] are equal
                boolean allValuesEqual = allTokenizedLabels.every { label -> label[i] == keyToCheck }

                // if so they can be trimmed
                if (allValuesEqual) {
                    labelPartsToSummarize.add([key: keys[i], value: keyToCheck])
                }
            }

            if (labelPartsToSummarize.size() > 0) {
                // trim labels
                graphs.each { it ->
                    removeCommonPartsFromLabel(it, labelPartsToSummarize)
                }
                // add trimmed part to commonLabel
                labelPartsToSummarize.each { part ->
                    String labelNewPart = "<b>" + part.key + "</b>: " + part.value.trim();
                    commonLabel == "" ? (commonLabel = labelNewPart) : (commonLabel += " | " + labelNewPart);
                }
            }
        }

        return new OsmRickshawChart(osmChartGraphs: graphs, osmChartGraphsCommonLabel: commonLabel)
    }


    OsmRickshawChart summarizeEventResultGraphs(List<OsmChartGraph> graphs) {
        String commonLabel = ""

        if(graphs.size() > 1) {
            def labelPartsToSummarize = [];

            // Split all Labels in individual parts
            List<List<String>> allTokenizedLabels = graphs*.label*.tokenize(HIGHCHART_LEGEND_DELIMITTER.trim())
            int labelPartCount = allTokenizedLabels[0].size()

            List<String> keys = [i18nService.msg('de.iteratec.result.measurand.label', 'Measurand'),
                                       i18nService.msg('job.jobGroup.label', 'Job Group'),
                                       i18nService.msg('de.iteratec.osm.result.measured-event.label', 'Measured step'),
                                       i18nService.msg('job.location.label', 'Location'),
                                       i18nService.msg('de.iteratec.osm.result.connectivity.label', 'Connectivity')]
            // special case: labelPartCount == 2
            if(labelPartCount == 2) {
                keys[1] = 'Identifier'
            }

            labelPartCount.times { i ->
                String keyToCheck = allTokenizedLabels[0][i]

                // check if all label parts[i] are equal
                boolean allValuesEqual = allTokenizedLabels.every { label -> label[i] == keyToCheck }

                // if so they can be trimmed
                if (allValuesEqual) {
                    labelPartsToSummarize.add([key: keys[i], value: keyToCheck])
                }
            }

            if (labelPartsToSummarize.size() > 0) {
                // trim labels
                graphs.each { it ->
                    removeCommonPartsFromLabel(it, labelPartsToSummarize)
                }
                // add trimmed part to commonLabel
                labelPartsToSummarize.each { part ->
                    String labelNewPart = "<b>" + part.key + "</b>: " + part.value.trim();
                    commonLabel == "" ? (commonLabel = labelNewPart) : (commonLabel += " | " + labelNewPart);
                }
            }
        }

        return new OsmRickshawChart(osmChartGraphs: graphs, osmChartGraphsCommonLabel: commonLabel)
    }


    private void removeCommonPartsFromLabel(OsmChartGraph graph, List<Map> labelPartsToSummarize) {
        labelPartsToSummarize.each { part ->
            graph.label = (graph.label - part.value.trim());
            graph.label = graph.label.replaceAll("[|]\\s+[|]", "|");
        }
        graph.label = graph.label.replaceFirst("^\\s+[|]\\s+", "");
        graph.label = graph.label.replaceFirst("\\s+[|]\\s+\$", "");
    }
}
