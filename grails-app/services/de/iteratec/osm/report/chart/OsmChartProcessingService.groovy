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

            def firstGraph = graphs.get(0);
            def tokenizedFirstGraphLabel = firstGraph.label.tokenize(HIGHCHART_LEGEND_DELIMITTER.trim());

            def labelParts = []

            if(tokenizedFirstGraphLabel.size() >= 1)
                labelParts.add([
                        key: i18nService.msg('job.jobGroup.label', 'Job Group'),
                        value: tokenizedFirstGraphLabel[0].trim() ]);
            if(tokenizedFirstGraphLabel.size() >= 2)
                labelParts.add([
                        key: i18nService.msg('de.iteratec.osm.result.measured-event.label', 'Measured step'),
                        value: tokenizedFirstGraphLabel[1].trim() ]);
            if(tokenizedFirstGraphLabel.size() >= 3)
                labelParts.add([
                        key: i18nService.msg('job.location.label', 'Location'),
                        value: tokenizedFirstGraphLabel[2].trim() ]);

            labelPartsToSummarize = labelParts.findAll { part ->
                graphs.every { it ->
                    it.label.contains(part.value.trim());
                }
            }

            graphs.each { it ->
                removeCommonPartsFromLabel(it, labelPartsToSummarize)
            }
            labelPartsToSummarize.each { part ->
                String labelNewPart = "<b>" + part.key + "</b>: " + part.value;
                commonLabel == "" ? (commonLabel = labelNewPart) : (commonLabel += " | " + labelNewPart);
            }
        }

        return new OsmRickshawChart(osmChartGraphs: graphs, osmChartGraphsCommonLabel: commonLabel)
    }

    OsmRickshawChart summarizeEventResultGraphs(List<OsmChartGraph> graphs) {

        List<Map> labelPartsToSummarize = [];
        String summary = "";

        if (graphs.size > 1) {

            OsmChartGraph firstGraph = graphs.get(0);
            List<String> tokenizedFirstGraphLabel = firstGraph.label.tokenize(HIGHCHART_LEGEND_DELIMITTER.trim());

            List<Map> labelParts = [
                    [ key: i18nService.msg('de.iteratec.result.measurand.label', 'Measurand'),
                      value: tokenizedFirstGraphLabel[0].trim() ]
            ]

            if(tokenizedFirstGraphLabel.size() == 5) {
                labelParts.add([
                        key: i18nService.msg('job.jobGroup.label', 'Job Group'),
                        value: tokenizedFirstGraphLabel[1].trim() ]);
                labelParts.add([
                        key: i18nService.msg('de.iteratec.osm.result.measured-event.label', 'Measured step'),
                        value: tokenizedFirstGraphLabel[2].trim() ]);
                labelParts.add([
                        key: i18nService.msg('job.location.label', 'Location'),
                        value: tokenizedFirstGraphLabel[3].trim() ]);
                labelParts.add([
                        key: i18nService.msg('de.iteratec.osm.result.connectivity.label', 'Connectivity'),
                        value: tokenizedFirstGraphLabel[4].trim() ]);
            }else if(tokenizedFirstGraphLabel.size() == 2) {
                labelParts.add([
                        key: 'Identifier',
                        value: tokenizedFirstGraphLabel[1].trim() ]);
            }

            labelPartsToSummarize = labelParts.findAll { part ->
                graphs.every { it ->
                    it.label.contains(part.value.trim());
                }
            }

            graphs.each { it ->
                removeCommonPartsFromLabel(it, labelPartsToSummarize)
            }

            labelPartsToSummarize.each { part ->
                String labelNewPart = "<b>" + part.key + "</b>: " + part.value;
                summary == "" ? (summary = labelNewPart) : (summary += " | " + labelNewPart);
            }
        }

        return new OsmRickshawChart(osmChartGraphs: graphs, osmChartGraphsCommonLabel: summary)
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
