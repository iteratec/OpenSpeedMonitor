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

package de.iteratec.osm.csi

import de.iteratec.osm.csi.transformation.DefaultTimeToCsMappingService
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.d3Data.*
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.util.I18nService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

/**
 * CsiConfigurationController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class CsiConfigurationController {

    I18nService i18nService
    DefaultTimeToCsMappingService defaultTimeToCsMappingService
    TimeToCsMappingService timeToCsMappingService

    def configurations() {
        CsiConfiguration config
        if (params.id) {
            config = CsiConfiguration.findById(params.id)
        } else {//There was no id defined
            config = CsiConfiguration.findByLabel("Default")
            if (!config) {//The Default Config is missing
                config = CsiConfiguration.findByIdGreaterThan(-1)
            }
        }
        if (!config) {//There is no Config at all or the id doesn't exists, redirect to create one
            //TODO redirect to a create page
            render ":("
            return
        }
        String selectedCsiConfigurationLabel = config.label
        log.debug(config.label)

        //Labels for charts
        String zeroWeightLabel = i18nService.msg("de.iteratec.osm.d3Data.treemap.zeroWeightLabel", "Pages ohne Gewichtung")
        String dataLabel = i18nService.msg("de.iteratec.osm.d3Data.treemap.dataLabel", "Page")
        String weightLabel = i18nService.msg("de.iteratec.osm.d3Data.treemap.weightLabel", "Gewichtung")
        String xAxisLabel = i18nService.msg("de.iteratec.osm.d3Data.barChart.xAxisLabel", "Tageszeit")
        String yAxisLabel = i18nService.msg("de.iteratec.osm.d3Data.barChart.yAxisLabel", "Gewichtung")
        String matrixViewXLabel = i18nService.msg("de.iteratec.osm.d3Data.matrixView.xLabel", "Browser")
        String matrixViewYLabel = i18nService.msg("de.iteratec.osm.d3Data.matrixView.yLabel", "Conn")
        String matrixViewWeightLabel = i18nService.msg("de.iteratec.osm.d3Data.matrixView.weightLabel", "Weight")
        String colorBrightLabel = i18nService.msg("de.iteratec.osm.d3Data.matrixView.colorBrightLabel", "less")
        String colorDarkLabel = i18nService.msg("de.iteratec.osm.d3Data.matrixView.colorDarkLabel", "more")
        String matrixZeroWeightLabel = i18nService.msg("de.iteratec.osm.d3Data.matrixView.zeroWeightLabel", "Im CSI nicht berücksichtigt")

        // arrange matrixViewData
        MatrixViewData matrixViewData = new MatrixViewData(weightLabel: matrixViewWeightLabel, rowLabel: matrixViewYLabel, columnLabel: matrixViewXLabel, colorBrightLabel: colorBrightLabel, colorDarkLabel: colorDarkLabel, zeroWeightLabel: matrixZeroWeightLabel)
        matrixViewData.addColumns(Browser.findAll()*.name as Set)
        matrixViewData.addRows(ConnectivityProfile.findAll()*.name as Set)
        config.browserConnectivityWeights.each {
            matrixViewData.addEntry(new MatrixViewEntry(weight: it.weight, columnName: it.browser.name, rowName: it.connectivity.name))
        }
        def matrixViewDataJSON = matrixViewData as JSON

        // arrange treemap data
        TreemapData treemapData = new TreemapData(zeroWeightLabel: zeroWeightLabel, dataName: dataLabel, weightName: weightLabel);
        config.pageWeights.each { pageWeight -> treemapData.addNode(new ChartEntry(name: pageWeight.page.name, weight: pageWeight.weight)) }
        def treemapDataJSON = treemapData as JSON

        // arrange barchart data
        BarChartData barChartData = new BarChartData(xLabel: xAxisLabel, yLabel: yAxisLabel)
        config.day.hoursOfDay.sort { a, b -> a.fullHour - b.fullHour }.each { h -> barChartData.addDatum(new ChartEntry(name: h.fullHour.toString(), weight: h.weight)) }
        def barChartJSON = barChartData as JSON

        // arrange default time to cs mapping chart data
        MultiLineChart defaultTimeToCsMappingsChart = defaultTimeToCsMappingService.getDefaultMappingsAsChart(10000)

        // arrange page time to cs mapping chart data
        MultiLineChart pageTimeToCsMappingsChart
        if (config.timeToCsMappings) {
            pageTimeToCsMappingsChart = timeToCsMappingService.getPageMappingsAsChart(10000, config)
        }

        List csi_configurations = []
        CsiConfiguration.list().each { csi_configurations << [it.id, it.label] }

        [errorMessagesCsi        : params.list('errorMessagesCsi'),
         showCsiWeights          : params.get('showCsiWeights') ?: false,
         mappingsToOverwrite     : params.list('mappingsToOverwrite'),
         csiConfigurations       : csi_configurations,
         selectedCsiConfiguration: selectedCsiConfigurationLabel,
         matrixViewData          : matrixViewDataJSON,
         treemapData             : treemapDataJSON,
         barchartData            : barChartJSON,
         defaultTimeToCsMappings : defaultTimeToCsMappingsChart as JSON,
         pageTimeToCsMappings    : pageTimeToCsMappingsChart as JSON,
         pages                   : Page.list()]
    }

    /**
     * Creates a copy of a csiConfiguration
     * @return redirects to the configurations view showing the created copy
     */
    def saveCopy() {
        if (CsiConfiguration.findByLabel(params.label)) {
            throw new IllegalArgumentException("CsiConfiguration already exists with name " + params.label)
        }

        CsiConfiguration sourceConfig = CsiConfiguration.findByLabel(params.sourceCsiConfigLabel)

        if(!sourceConfig) {
            throw new IllegalArgumentException("no csi configuration with name " + params.sourceCsiConfigLabel + " found")
        }

        CsiConfiguration newCsiConfig = CsiConfiguration.copyConfiguration(sourceConfig)
        newCsiConfig.label = params.label
        newCsiConfig.save(failOnError: true, flush: true)

        redirect(action: 'configurations', params: [id: newCsiConfig.id])
    }

    /**
     * Deletes a selected csiConfiguration and updates the csiConfiguration (set it to null) in involved jobGroups
     * It has to be ensured, that after deleting there is still a csi configuration left
     * @return
     */
    def deleteCsiConfiguration() {
        CsiConfiguration configToDelete = CsiConfiguration.findByLabel(params.label)

        if (CsiConfiguration.count <= 1) {
            throw new IllegalStateException("There has to be a csiConfiguration left after deleting")
        } else if (!configToDelete) {
            throw new IllegalArgumentException("There is no csiConfiguration with label: " + params.label)
        }


        List<JobGroup> jobGroupsUsingCsiConfigurationToDelete = JobGroup.findAllByCsiConfiguration(configToDelete)
        jobGroupsUsingCsiConfigurationToDelete.each { group ->
            group.csiConfiguration = null
            group.save()
        }

        configToDelete.delete()
        redirect(action: 'configurations')
    }

    /**
     * You are allowed to delete a csi configuration only if
     *  there is at least one other csiConfiguration left after deleting
     */
    def validateDeletion() {
        int csiConfigurationCount = CsiConfiguration.count()
        List<String> errorMessages = new ArrayList<>()

        if (csiConfigurationCount <= 1) {
            errorMessages.add(i18nService.msg("de.iteratec.osm.csiConfiguration.deleteLastCsiConfigurationError", "es muessen mindenst zwei CsiConfigurations vorhanden sein"))
        }

        def jsonResponse = [errorMessages: errorMessages]
        render jsonResponse as JSON
    }

    /**
     * Searches for jobGroups which are using a csiConfiguration
     * @param csiConfigurationLabel the label of the csiConfiguration
     * @return a list of all jobGroups using the given csiConfiguration
     */
    def getJobGroupsUsingCsiConfiguration(String csiConfigurationLabel) {
        CsiConfiguration conf = CsiConfiguration.findByLabel(csiConfigurationLabel)

        List<JobGroup> jobGroupsUsingCsiConfiguration = JobGroup.findAllByCsiConfiguration(conf)
        List<String> jobGroupNames = jobGroupsUsingCsiConfiguration*.name

        def jsonResponse = [jobGroupNames: jobGroupNames]
        render jsonResponse as JSON
    }

    /**
     * Deletes a defaultTimeToCsMapping
     * @param name the name of the defaultTimeToCsMapping
     */
    @Secured(['ROLE_SUPER_ADMIN'])
    def deleteDefaultCsiMapping(String name) {
        defaultTimeToCsMappingService.deleteDefaultTimeToCsMapping(name)
        redirect action:'configurations'
    }
}
