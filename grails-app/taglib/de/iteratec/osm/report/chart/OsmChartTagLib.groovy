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

import de.iteratec.osm.ConfigService

class OsmChartTagLib {

    ConfigService configService
    static namespace = "iteratec"

    def timeSeriesChart = { attrs, body ->
        String divId = attrs["divId"] ?: "graph_container"
        Boolean isAggregatedData = attrs["isAggregatedData"]
        List<OsmChartGraph> data = attrs["data"]
        String title = attrs["title"] ?: ""
        String labelSummary = attrs["labelSummary"]
        Boolean markerEnabled = attrs['markerEnabled'] != null ? Boolean.valueOf(attrs['markerEnabled']) : false
        Boolean dataLabelsActivated = attrs['dataLabelsActivated'] != null ? Boolean.valueOf(attrs['dataLabelsActivated']) : false
        List<OsmChartAxis> yAxesLabels = attrs['highChartLabels']
        String heightOfChart = attrs["heightOfChart"]
        String width = attrs["width"]
        List annotations = attrs["annotations"]
        String yAxisMax = attrs["yAxisMax"] ?: "auto"
        String yAxisMin = attrs["yAxisMin"] = attrs['yAxisMin'] ?: '0'
        String downloadPngLabel = attrs["downloadPngLabel"]

        def htmlCreater = new RickshawHtmlCreater()
        out << htmlCreater.generateHtmlForMultipleYAxisGraph(divId, data, dataLabelsActivated, heightOfChart, width,
                yAxesLabels, title, labelSummary, markerEnabled, annotations, yAxisMin, yAxisMax, downloadPngLabel,
                isAggregatedData)

        return out.toString()
    }
}
