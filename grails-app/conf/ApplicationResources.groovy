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

import de.iteratec.osm.report.chart.ChartingLibrary

modules = {

    // components /////////////////////////////////////////////////////////////////////////////////

    /**
     * Is set in main-layout grails-app/views/layouts/kickstart_osm.gsp.
     */
    'core' {
        dependsOn "jquery"
        dependsOn "bootstrap"
        dependsOn "bootstrap_utils"

        resource url:'js/application.js'
        resource url:'css/application.css'
    }
    /**
     * Date and timepicker-logic for dashboards
     */
    'osm-dashboard-datetime' {
        defaultBundle 'osm-dashboard'
        resource url: 'js/date-time-picker/osm-date-time-picker.js'
        resource url: 'js/date-time-picker/bootstrap-timepicker.min.js'
        //for debugging:
        //		resource url: 'js/date-time-picker/bootstrap-timepicker.js'
    }
    /**
     * Charting-stuff (external library-code and osm-specifics).
     */
    'iteratec-chart' {
        defaultBundle 'osm-dashboard'

        if (grailsApplication.config.grails.de.iteratec.osm.report.chart.availableChartTagLibs.contains(ChartingLibrary.HIGHCHARTS)) {
            resource url: 'js/highcharts.js'
            resource url: 'js/highchart-taglib.js'
            resource url: 'js/exporting.js'
            resource url: 'js/charts/rgbcolor.js'
            resource url: 'js/charts/StackBlur.js'
            resource url: 'js/charts/canvg.js'
        }
        if (grailsApplication.config.grails.de.iteratec.osm.report.chart.availableChartTagLibs.contains(ChartingLibrary.RICKSHAW)) {
            resource url: 'js/d3/d3.v3.js'
            resource url: 'css/rickshaw/rickshaw_custom.css'
            resource url: 'js/rickshaw/rickshaw_custom.js'
            resource url: 'js/rickshaw/rickshawChartCreation.js'
            resource url: 'js/rickshaw/html2canvas.js'
            resource url: 'js/rickshaw/html2canvas.svg.js'
        }

        resource url: 'js/charts/chart-export.js'
        resource url: 'js/charts/chart-adjustment.js'
    }
    /**
     * Just a wrapper for date and timepicker-logic and charting-stuff for dashboards.
     */
    'osm-dashboard-datetime-and-chart' {
        dependsOn 'osm-dashboard-datetime'
        dependsOn 'iteratec-chart'
    }
    /**
     * For template grails-app/views/eventResultDashboard/_selectMeasurings
     */
    'select-measurings' {
        defaultBundle 'osm-dashboard'
        resource url: 'js/select-measurings.js'
    }
    'tagit' {
        dependsOn 'jquery-ui'

        resource 'css/jquery.tagit.css'
        resource 'css/jquery-ui-smoothness.css'
        resource 'js/tag-it.min.js'
    }

    'prettycron' {
        resource 'js/prettycron/later.min.js'
        resource 'js/prettycron/moment.min.js'
        resource 'js/prettycron/prettycron.js'
        resource 'js/prettycron/cron-expressions.js'
    }
    'codemirror' {
        resource '/css/codemirror/codemirror.css'
        resource '/css/codemirror/warnings.css'
        resource '/css/hint/show-hint.css'
        resource '/js/codemirror/codemirror.js'
        resource '/js/codemirror/hint/show-hint.js'
        resource '/js/codemirror/hint/pts-hint.js'
        resource '/js/codemirror/mode/pts.js'
    }
    'table-fixed-header' {
        resource '/css/table-fixed-header/table-fixed-header.css'
        resource '/js/table-fixed-header/table-fixed-header.js'
    }
    'joblist' {
        dependsOn 'table-fixed-header'
        resource '/css/job/list.css'
        resource '/js/job/list.js'
    }
    'jobedit' {
        resource '/css/job/edit.css'
        resource '/js/job/edit.js'
    }
    'queuestatus' { resource '/css/queueStatus/list.css' }
    'timeago' { resource '/js/timeago/jquery.timeago.js' }
    'timeago-de' {
        dependsOn 'timeago'
        resource '/js/timeago/jquery.timeago.de.js'
    }
    'future-only-timeago' {
        dependsOn 'timeago'
        resource '/js/timeago/future-only-timeago.js'
    }
    'spin' { resource '/js/spin/spin.min.js' }
    'chosen' {
        resource 'css/chosen/chosen.css'
        resource 'js/chosen/chosen.jquery.min.js'
        //for debugging:
        //		resource 'js/chosen/chosen.jquery.js'
    }

    // bundles to use in views /////////////////////////////////////////////////////////////////////////////////

    'eventresult-dashboard' {
        dependsOn 'osm-dashboard-datetime-and-chart'

        resource url: 'js/eventresultdashboard/showAll.js'
    }
    'csi-dashboard' {
        dependsOn 'osm-dashboard-datetime-and-chart'

        resource url: 'js/csidashboard/showAll.js'
    }
    'eventresult' {
        dependsOn 'osm-dashboard-datetime'

        resource url: 'js/eventresult/listResult.js'
    }
}