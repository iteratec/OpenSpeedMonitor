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
import de.iteratec.osm.util.PerformanceLoggingService
import grails.converters.JSON

/**
 * CsiConfigurationController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class CsiConfigurationController {

    I18nService i18nService
    DefaultTimeToCsMappingService defaultTimeToCsMappingService
    TimeToCsMappingService timeToCsMappingService
    PerformanceLoggingService performanceLoggingService

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
        List<Page> csiConfigPages = config.pageWeights*.page
        Page.list().each { page ->
            if (!csiConfigPages.contains(page)) {
                treemapData.addNode(new ChartEntry(name: page.name, weight: 0))
            }
        }
        config.pageWeights.each { pageWeight -> treemapData.addNode(new ChartEntry(name: pageWeight.page.name, weight: pageWeight.weight)) }
        def treemapDataJSON = treemapData as JSON

        // arrange barchart data
        BarChartData barChartData = new BarChartData(xLabel: xAxisLabel, yLabel: yAxisLabel)
        (0..23).each {
            barChartData.addDatum(new ChartEntry(name: it, weight: config.csiDay.getHourWeight(it)))
        }
        def barChartJSON = barChartData as JSON

        // arrange default time to cs mapping chart data
        MultiLineChart defaultTimeToCsMappingsChart = defaultTimeToCsMappingService.getDefaultMappingsAsChart(10000)

        // arrange page time to cs mapping chart data
        MultiLineChart pageTimeToCsMappingsChart
        boolean pageMappingsExist = false;
        if (config.timeToCsMappings) {
            pageTimeToCsMappingsChart = timeToCsMappingService.getPageMappingsAsChart(10000, config)
            pageMappingsExist = true;
        } else {
            pageTimeToCsMappingsChart = new MultiLineChart()
        }

        List csi_configurations = []
        CsiConfiguration.list().each { csi_configurations << ['id': it.id, 'label': it.label] }

        [errorMessagesCsi        : params.list('errorMessagesCsi'),
         showCsiWeights          : params.get('showCsiWeights') ?: false,
         mappingsToOverwrite     : params.list('mappingsToOverwrite'),
         csiConfigurations       : csi_configurations,
         selectedCsiConfiguration: config,
         matrixViewData          : matrixViewDataJSON,
         treemapData             : treemapDataJSON,
         barchartData            : barChartJSON,
         defaultTimeToCsMappings : defaultTimeToCsMappingsChart as JSON,
         pageTimeToCsMappings    : pageTimeToCsMappingsChart as JSON,
         pages                   : Page.list(),
         pageMappingsExist       : pageMappingsExist]
    }

    /**
     * Creates a copy of a csiConfiguration
     * @return redirects to the configurations view showing the created copy
     */
    def saveCopy() {

        CsiConfiguration sourceConfig

        performanceLoggingService.logExecutionTime(
                PerformanceLoggingService.LogLevel.DEBUG,
                "Getting source CSI Configuration",
                PerformanceLoggingService.IndentationDepth.ONE
        ) {
            if (CsiConfiguration.findByLabel(params.label)) {
                throw new IllegalArgumentException("CsiConfiguration already exists with name " + params.label)
            }

            sourceConfig = CsiConfiguration.findByLabel(params.sourceCsiConfigLabel)

            if (!sourceConfig) {
                throw new IllegalArgumentException("no csi configuration with name " + params.sourceCsiConfigLabel + " found")
            }
        }

        CsiConfiguration newCsiConfig
        performanceLoggingService.logExecutionTime(
                PerformanceLoggingService.LogLevel.DEBUG,
                "coppy CSI Configuration",
                PerformanceLoggingService.IndentationDepth.ONE
        ) {
            newCsiConfig = CsiConfiguration.copyConfiguration(sourceConfig)
        }

        performanceLoggingService.logExecutionTime(
                PerformanceLoggingService.LogLevel.DEBUG,
                "saave copieed CSI Configuration",
                PerformanceLoggingService.IndentationDepth.ONE
        ) {
            newCsiConfig.label = params.label
            newCsiConfig.save(failOnError: true, flush: true)
        }

        performanceLoggingService.logExecutionTime(
                PerformanceLoggingService.LogLevel.DEBUG,
                "redirect",
                PerformanceLoggingService.IndentationDepth.ONE
        ) {
            List csi_configurations = []
            CsiConfiguration.list().each { csi_configurations << ['id': it.id, 'label': it.label] }
            render([
                    'newCsiConfigLabel'   : newCsiConfig.label,
                    'newCsiConfigId'      : newCsiConfig.ident(),
                    'allCsiConfigurations': csi_configurations
            ] as JSON)
        }
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
    def deleteDefaultCsiMapping(String name) {
        defaultTimeToCsMappingService.deleteDefaultTimeToCsMapping(name)
        redirect action: 'configurations'
    }

    def applyNewMappingToPage(ApplyMappingCommand applyMappingCommand) {
        defaultTimeToCsMappingService.copyDefaultMappingToPage(applyMappingCommand.getPage(),
                applyMappingCommand.getDefaultMappingName(), applyMappingCommand.getCsiConfiguration())
        render ""
    }

    /**
     * Updates label and description for given {@link CsiConfiguration}.
     * @param csiConfId
     *          ID of {@link CsiConfiguration} to update.
     * @param csiConfNewLabel
     *          New label to set. May not be null.
     * @param csiConfNewDescription
     *          New description to set.
     * @return Plain text response.
     */
    def updateConfiguration(Long csiConfId, String csiConfNewLabel, String csiConfNewDescription) {

        response.setContentType('text/plain;charset=UTF-8')

        CsiConfiguration csiConf = CsiConfiguration.get(csiConfId)

        if (csiConf == null) {
            response.status = 404
            render i18nService.msg(
                    'de.iteratec.osm.csi.configuration.update.error.wrong-id',
                    "No CSIConfiguration found for ID '${csiConfId}'",
                    [csiConfId]
            )
            return null
        }
        if (csiConfNewLabel == null) {
            response.status = 400
            render i18nService.msg(
                    'de.iteratec.osm.csi.configuration.update.error.null-label',
                    "Label to set for new CSI configuration may not be null."
            )
            return null
        }
        if (csiConf.label != csiConfNewLabel && CsiConfiguration.findByLabel(csiConfNewLabel)) {
            response.status = 400
            render i18nService.msg(
                    'de.iteratec.osm.csi.configuration.update.error.label-already-exist',
                    "A CSI Configuration with new label '${csiConfNewLabel}' already exists.",
                    [csiConfNewLabel]
            )
            return null
        }

        csiConf.label = csiConfNewLabel
        csiConf.description = csiConfNewDescription
        csiConf.save(failOnError: true)

        response.status = 200
        render "Updated CSI configuration successfully."

    }

    def removePageMapping() {

        response.setContentType('text/plain;charset=UTF-8')

        Page pageToRemoveMappingFrom = Page.findByName(params.pageName)
        CsiConfiguration csiConfigurationToRemovePageMappingFrom = CsiConfiguration.get(params.csiConfId)

        if (pageToRemoveMappingFrom == null) {
            response.status = 404
            render i18nService.msg(
                    'de.iteratec.osm.csi.configuration.remove-page-mapppings.page-doesnt-exist',
                    "A Page with name ${params.pageName} doesn't exist.",
                    [params.pageName]
            )
            return null
        }
        if (csiConfigurationToRemovePageMappingFrom == null) {
            response.status = 404
            render i18nService.msg(
                    'de.iteratec.osm.csi.configuration.remove-page-mapppings.csiconf-doesnt-exist',
                    "A CSI Configuration with ID ${params.csiConfId} doesn't exist.",
                    [params.csiConfId]
            )
            return null
        }
        log.info("Mappings for page '${pageToRemoveMappingFrom.name}' should be removed from csi configuration '${csiConfigurationToRemovePageMappingFrom.label}'")
        List<TimeToCsMapping> toDelete = []
        toDelete.addAll(csiConfigurationToRemovePageMappingFrom.timeToCsMappings.findAll {
            it.page.ident() == pageToRemoveMappingFrom.ident()
        })
        if (toDelete.size() == 0) {
            response.status = 404
            render i18nService.msg(
                    'de.iteratec.osm.csi.configuration.remove-page-mapppings.csiconf-doesnt-has-mappings-for-page',
                    "CSI Configuration with ID ${params.csiConfId} doesn't contain any page mappings for page with name ${params.pageName}.",
                    [params.csiConfId, params.pageMappingId]
            )
            return null
        }

        log.info("Delete ${toDelete.size()} Mappings...")
        toDelete.each { mappingToDelete ->
            csiConfigurationToRemovePageMappingFrom.removeFromTimeToCsMappings(mappingToDelete)
            mappingToDelete.delete()
        }
        log.info("...DONE")

        response.status = 200
        String successMessage = i18nService.msg(
                'de.iteratec.osm.csi.configuration.remove-page-mapppings.success.msg',
                "Removed ${toDelete.size()} Mappings of page ${pageToRemoveMappingFrom.name} from CsiConfiguration ${csiConfigurationToRemovePageMappingFrom.label}.",
                [toDelete.size(), pageToRemoveMappingFrom.name, csiConfigurationToRemovePageMappingFrom.label]
        )
        performanceLoggingService.logExecutionTime(PerformanceLoggingService.LogLevel.DEBUG, "render mappings", PerformanceLoggingService.IndentationDepth.ONE) {
            render successMessage
        }

    }
}

class ApplyMappingCommand {

    String defaultMappingName
    Long csiConfigurationId
    Long pageId


    static constraints = {
        defaultMappingName(blank: false)
        csiConfigurationId(nullable: false, min: 1l)
        pageId(nullable: false, min: 1l)
    }

    public Page getPage() {
        return Page.findById(pageId)
    }

    public CsiConfiguration getCsiConfiguration() {
        return CsiConfiguration.findById(csiConfigurationId)
    }

}
