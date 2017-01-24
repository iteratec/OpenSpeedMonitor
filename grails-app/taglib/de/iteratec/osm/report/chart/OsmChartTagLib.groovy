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

    def singleYAxisChart = { attrs, body ->

        List<OsmChartGraph> data = attrs["data"];
        String title = attrs["title"]
        String labelSummary = attrs["labelSummary"];
        String lineType = attrs["lineType"]
        String yType = attrs["yType"]
        String width = attrs["width"]
        String yAxisMax = attrs["yAxisMax"] ?: "auto"
        String divId = attrs["divId"] ?: UUID.randomUUID().toString();
        String measurementUnit = attrs['measurementUnit'] ?: 'unkown'
        Long xAxisMin = attrs['xAxisMin'] ? Long.valueOf(attrs['xAxisMin']) : null
        Long xAxisMax = attrs['xAxisMax'] ? Long.valueOf(attrs['xAxisMax']) : null
        Boolean markerEnabled = attrs['markerEnabled'] != null ? Boolean.valueOf(attrs['markerEnabled']) : false
        Boolean dataLabelsActivated = attrs['dataLabelsActivated'] != null ? Boolean.valueOf(attrs['dataLabelsActivated']) : false
        Boolean yAxisScalable = attrs['yAxisScalable'] != null ? Boolean.valueOf(attrs['yAxisScalable']) : false
        String yAxisMin = attrs["yAxisMin"] = attrs['yAxisMin'] ?: '0'
        String lineWidthGlobal = attrs["lineWidthGlobal"] = attrs['lineWidthGlobal'] ?: '2'
        Boolean optimizeForExport = attrs['optimizeForExport'] != null ? Boolean.valueOf(attrs['optimizeForExport']) : false
        Boolean openDatapointLinksInNewWindow = attrs['openDataPointLinksInNewWindow'] != null ? Boolean.valueOf(attrs['openDataPointLinksInNewWindow']) : true
        List annotations = attrs["annotations"]
        String heightOfChart = attrs["heightOfChart"]
        String downloadPngLabel = attrs["downloadPngLabel"]

        data.each {
            it.measurandGroup = MeasurandGroup.PERCENTAGES
        }
        List<OsmChartAxis> highChartLabels = new LinkedList<OsmChartAxis>();
        highChartLabels.add(new OsmChartAxis(yType, MeasurandGroup.PERCENTAGES, "", 1, OsmChartAxis.LEFT_CHART_SIDE))

        def htmlCreater = new RickshawHtmlCreater()
        out << htmlCreater.generateHtmlForMultipleYAxisGraph(divId, data, dataLabelsActivated, heightOfChart, width, highChartLabels, title, labelSummary, markerEnabled, annotations, yAxisMin, yAxisMax, downloadPngLabel)

        return out.toString()
    }

    def multipleAxisChart =
            { attrs, body ->

                List<OsmChartGraph> data = attrs["data"];
                String title = attrs["title"]
                String labelSummary = attrs["labelSummary"];
                String lineType = attrs["lineType"]
                String width = attrs["width"]
                String yAxisMax = attrs["yAxisMax"] ?: "auto"
                String divId = attrs["divId"] ?: UUID.randomUUID().toString();
                String measurementUnits = attrs['measurementUnit'] ?: 'unkown'
                Long xAxisMin = attrs['xAxisMin'] ? Long.valueOf(attrs['xAxisMin']) : null
                Long xAxisMax = attrs['xAxisMax'] ? Long.valueOf(attrs['xAxisMax']) : null
                Boolean markerEnabled = attrs['markerEnabled'] != null ? Boolean.valueOf(attrs['markerEnabled']) : false
                Boolean dataLabelsActivated = attrs['dataLabelsActivated'] != null ? Boolean.valueOf(attrs['dataLabelsActivated']) : false
                Boolean yAxisScalable = attrs['yAxisScalable'] != null ? Boolean.valueOf(attrs['yAxisScalable']) : false
                String yAxisMin = attrs["yAxisMin"] = attrs['yAxisMin'] ?: '0'
                String lineWidthGlobal = attrs["lineWidthGlobal"] = attrs['lineWidthGlobal'] ?: '2'
                Boolean optimizeForExport = attrs['optimizeForExport'] != null ? Boolean.valueOf(attrs['optimizeForExport']) : false
                Long defaultThreshold = 100000
                Long highChartsTurboThreshold = attrs['highChartsTurboThreshold'] ? Long.valueOf(attrs['highChartsTurboThreshold']) : defaultThreshold
                Boolean openDatapointLinksInNewWindow = attrs['openDatapointLinksInNewWindow'] != null ? Boolean.valueOf(attrs['openDatapointLinksInNewWindow']) : true
                List<OsmChartAxis> yAxesLabels = attrs['highChartLabels']
                List annotations = attrs["annotations"]
                String downloadPngLabel = attrs["downloadPngLabel"]

                if (title == null) {
                    title = "";
                }

                String heightOfChart = attrs["heightOfChart"]

                def htmlCreater = new RickshawHtmlCreater()
                out << htmlCreater.generateHtmlForMultipleYAxisGraph(divId, data, dataLabelsActivated, heightOfChart, width, yAxesLabels, title, labelSummary, markerEnabled, annotations, yAxisMin, yAxisMax, downloadPngLabel)

                return out.toString()
            }

    def csiMappingChart = { attrs, body ->

        String chartIdentifier = attrs['chartIdentifier']
        String bottomOffsetXAxis = attrs['bottomOffsetXAxis']
        String yAxisRightOffset = attrs['yAxisRightOffset']
        String chartBottomOffset = attrs['chartBottomOffset']
        String yAxisTopOffset = attrs['yAxisTopOffset']
        String bottomOffsetLegend = attrs['bottomOffsetLegend']

        def htmlCreater = new RickshawHtmlCreater()
        out << htmlCreater.generateCsiMappingsChartHtml(chartIdentifier, bottomOffsetXAxis, yAxisRightOffset,
                chartBottomOffset, yAxisTopOffset, bottomOffsetLegend)

        return out.toString()
    }

}
