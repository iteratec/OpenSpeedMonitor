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

            checkIfAllLabelsHaveEqualSectionCount(1,4, graphs)

            final List<String> graphLabelSectionKeys = [i18nService.msg('de.iteratec.osm.csi.type.heading', 'Csi Type'),
                                                        i18nService.msg('job.jobGroup.label', 'Job Group'),
                                       i18nService.msg('de.iteratec.osm.result.measured-event.label', 'Measured step'),
                                       i18nService.msg('job.location.label', 'Location')]

            List labelPartsToSummarize = getLabelPartsToSummarize(graphs, graphLabelSectionKeys)

            if (labelPartsToSummarize.size() > 0) {

                graphs.each { it ->
                    removeCommonPartsFromLabel(it, labelPartsToSummarize)
                }
                commonLabel = createCommonLabel(labelPartsToSummarize)

            }
        }

        return new OsmRickshawChart(osmChartGraphs: graphs, osmChartGraphsCommonLabel: commonLabel)
    }


    OsmRickshawChart summarizeEventResultGraphs(List<OsmChartGraph> graphs) {
        String commonLabel = ""

        if(graphs.size() > 1) {

            checkIfAllLabelsHaveEqualSectionCount(5,5, graphs)

            List<String> graphLabelSectionKeys = [i18nService.msg('de.iteratec.result.measurand.label', 'Measurand'),
                                 i18nService.msg('job.jobGroup.label', 'Job Group'),
                                 i18nService.msg('de.iteratec.osm.result.measured-event.label', 'Measured step'),
                                 i18nService.msg('job.location.label', 'Location'),
                                 i18nService.msg('de.iteratec.osm.result.connectivity.label', 'Connectivity')]

            List labelPartsToSummarize = getLabelPartsToSummarize(graphs, graphLabelSectionKeys)

            if (labelPartsToSummarize.size() > 0) {

                graphs.each { it ->
                    removeCommonPartsFromLabel(it, labelPartsToSummarize)
                }
                commonLabel = createCommonLabel(labelPartsToSummarize)

            }
        }

        return new OsmRickshawChart(osmChartGraphs: graphs, osmChartGraphsCommonLabel: commonLabel)
    }

    void checkIfAllLabelsHaveEqualSectionCount(int min, int max, List<OsmChartGraph> graphs) {
        // check if all equal
        List <List> labels = splitGraphLabels(graphs)
        int firstLabelSectionCount = labels[0].size()
        if(!labels.every{label -> label.size() == firstLabelSectionCount}) {
            IllegalArgumentException exception = new IllegalArgumentException("Labels have different section counts")
            log.error("Illegal Argument", exception)
            throw exception
        }
        //check if all in range
        if(firstLabelSectionCount < min || firstLabelSectionCount > max) {
            IllegalArgumentException exception = new IllegalArgumentException("Section count has to be in range [" + min + "," + max + "]")
            log.error("Illegal Argument", exception)
            throw exception
        }
    }

    private List getLabelPartsToSummarize(List<OsmChartGraph> graphs, List<String> graphLabelSectionKeys) {

        List<List<String>> tokenizedLabelsOfAllGraphs = splitGraphLabels(graphs)

        List labelPartsToSummarize = []

        tokenizedLabelsOfAllGraphs[0].size().times { labelSectionIndex ->

            String currentLabelSection = tokenizedLabelsOfAllGraphs[0][labelSectionIndex]

            // check if all label parts[i] are equal
            boolean allValuesEqual = tokenizedLabelsOfAllGraphs.every { label -> label[labelSectionIndex] == currentLabelSection }

            // if so they can be trimmed
            if (allValuesEqual) {
                labelPartsToSummarize.add([key: graphLabelSectionKeys[labelSectionIndex], value: currentLabelSection])
            }

        }
        return labelPartsToSummarize
    }

    private String createCommonLabel(ArrayList labelPartsToSummarize) {
        String commonLabel = ''
        labelPartsToSummarize.each { part ->
            String labelNewPart = "<b>" + part.key + "</b>: " + part.value.trim();
            commonLabel == "" ? (commonLabel = labelNewPart) : (commonLabel += " | " + labelNewPart);

        }
        return commonLabel
    }

    private List splitGraphLabels(List<OsmChartGraph> graphs) {
        List<List<String>> allTokenizedLabels = graphs*.label*.tokenize(HIGHCHART_LEGEND_DELIMITTER.trim())
        return allTokenizedLabels
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
