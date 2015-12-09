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

import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.dao.BrowserDaoService
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.dao.PageDaoService
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.web.multipart.MultipartFile

/**
 * Provides Actions for Download and Upload of csi configurations like weighting or mapping data.
 */
class CsiConfigIOController {

    PageDaoService pageDaoService
    BrowserDaoService browserDaoService
    CustomerSatisfactionWeightService customerSatisfactionWeightService

    // export ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    def downloadBrowserWeights() {
        DateTimeFormatter dtFormater = DateTimeFormat.forPattern("yyyyMMdd")
        response.setHeader("Content-disposition",
                "attachment; filename=${dtFormater.print(new DateTime())}browser_weights.csv")
        response.contentType = "text/csv"
        StringBuilder builder = new StringBuilder()
        builder.append('name;weight\n')
        browserDaoService.findAll().each {
            builder.append("${it.name};${it.weight}\n")
        }
        response.outputStream << builder.toString()
    }

    def downloadBrowserConnectivityWeights() {
        DateTimeFormatter dtFormater = DateTimeFormat.forPattern("yyyyMMdd")
        response.setHeader("Content-disposition",
                "attachment; filename=${dtFormater.print(new DateTime())}browser_connectivity_weights.csv")
        response.contentType = "text/csv"
        StringBuilder builder = new StringBuilder()
        builder.append('browser;connectivity;weight\n')
        Browser.findAll().each { browser ->
            ConnectivityProfile.findAll().each { connectivity ->
                BrowserConnectivityWeight weight = BrowserConnectivityWeight.findByBrowserAndConnectivity(browser, connectivity)
                if(weight) {
                    builder.append("${browser.name};${connectivity.name};${weight.weight}\n")
                } else {
                    builder.append("${browser.name};${connectivity.name};\n")
                }
            }
        }

        response.outputStream << builder.toString()
    }

    def downloadPageWeights() {
        DateTimeFormatter dtFormater = DateTimeFormat.forPattern("yyyyMMdd")
        response.setHeader("Content-disposition",
                "attachment; filename=${dtFormater.print(new DateTime())}page_weights.csv")
        response.contentType = "text/csv"
        StringBuilder builder = new StringBuilder()
        builder.append('name;weight\n')
        pageDaoService.findAll().each {
            builder.append("${it.name};${it.weight}\n")
        }
        response.outputStream << builder.toString()
    }

    def downloadHourOfDayWeights() {
        DateTimeFormatter dtFormater = DateTimeFormat.forPattern("yyyyMMdd")
        response.setHeader("Content-disposition",
                "attachment; filename=${dtFormater.print(new DateTime())}HourOfDays_weights.csv")
        response.contentType = "text/csv"
        StringBuilder builder = new StringBuilder()
        builder.append('fullHour;weight\n')
        // FIXME Change to use a DAO
        HourOfDay.list().each {
            builder.append("${it.fullHour};${it.weight}\n")
        }
        response.outputStream << builder.toString()
    }

    def downloadDefaultTimeToCsMappings(){
        DateTimeFormatter dtFormater = DateTimeFormat.forPattern("yyyyMMdd")
        response.setHeader("Content-disposition",
                "attachment; filename=${dtFormater.print(new DateTime())}defaultTimeToCsMappings.csv")
        response.contentType = "text/csv"
        StringBuilder builder = new StringBuilder()
        builder.append('name;loadTimeInMilliSecs;customerSatisfactionInPercent\n')
        // FIXME Change to use a DAO
        DefaultTimeToCsMapping.list().each {
            builder.append("${it.name};${it.loadTimeInMilliSecs};${it.customerSatisfactionInPercent}\n")
        }
        response.outputStream << builder.toString()
    }

    // import ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    def uploadBrowserWeights() {
        MultipartFile csv = request.getFile('browserCsv')
        List<String> errorMessagesCsiValidation = customerSatisfactionWeightService.validateWeightCsv(WeightFactor.BROWSER, csv.getInputStream())
        if (!errorMessagesCsiValidation) {
            customerSatisfactionWeightService.persistNewWeights(WeightFactor.BROWSER, csv.getInputStream())
        }
        CsiDashboardController.log.info("errorMessagesCsiValidation=$errorMessagesCsiValidation")
        redirect(controller: 'CsiDashboard',
                action: 'weights',
                params: [errorMessagesCsi: errorMessagesCsiValidation])
    }

    def uploadBrowserConnectivityWeights() {
        MultipartFile csv = request.getFile('browserConnectivityCsv')
        List<String> errorMessagesCsiValidation = customerSatisfactionWeightService.validateWeightCsv(WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, csv.getInputStream())
        if (!errorMessagesCsiValidation) {
            customerSatisfactionWeightService.persistNewWeights(WeightFactor.BROWSER_CONNECTIVITY_COMBINATION, csv.getInputStream())
        }
        CsiDashboardController.log.info("errorMessagesCsiValidation=$errorMessagesCsiValidation")
        redirect(controller: 'CsiDashboard',
                action: 'weights',
                params: [errorMessagesCsi: errorMessagesCsiValidation])
    }

    def uploadPageWeights() {
        MultipartFile csv = request.getFile('pageCsv')
        List<String> errorMessagesCsiValidation = customerSatisfactionWeightService.validateWeightCsv(WeightFactor.PAGE, csv.getInputStream())
        if (!errorMessagesCsiValidation) {
            customerSatisfactionWeightService.persistNewWeights(WeightFactor.PAGE, csv.getInputStream())
        }
        redirect(controller: 'CsiDashboard',
                action: 'weights',
                params: [errorMessagesCsi: errorMessagesCsiValidation])
    }

    def uploadHourOfDayWeights() {
        MultipartFile csv = request.getFile('hourOfDayCsv')
        List<String> errorMessagesCsiValidation = customerSatisfactionWeightService.validateWeightCsv(WeightFactor.HOUROFDAY, csv.getInputStream())
        if (!errorMessagesCsiValidation) {
            customerSatisfactionWeightService.persistNewWeights(WeightFactor.HOUROFDAY, csv.getInputStream())
        }
        redirect(controller: 'CsiDashboard',
                action: 'weights',
                params: [errorMessagesCsi: errorMessagesCsiValidation])
    }

    def uploadDefaultTimeToCsMappings(){
        
    }

}
