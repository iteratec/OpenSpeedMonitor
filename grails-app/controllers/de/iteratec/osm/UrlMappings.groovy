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

package de.iteratec.osm

/**
 * Base-URL-mappings for the application openSpeedMonitor.
 * @author nkuhn
 *
 */
class UrlMappings {

    static mappings = {

        ////////////////////////////////////////////////////////////////////////////////////////////////
        // Landing page
        ////////////////////////////////////////////////////////////////////////////////////////////////
        '/'(controller: 'landing')

        ////////////////////////////////////////////////////////////////////////////////////////////////
        // Pages without controller
        ////////////////////////////////////////////////////////////////////////////////////////////////
        "/releasenotes"(view: "/siteinfo/releasenotes")
        "/about"(view: "/siteinfo/about")
        "/systeminfo"(view: "/siteinfo/systeminfo")
        "/applicationDashboard/rest/$action"(controller: "applicationDashboard")
        "/applicationDashboard/**?"(view: "/applicationDashboard/index")

        ////////////////////////////////////////////////////////////////////////////////////////////////
        // Pages with controller
        // WARN: No domain/controller should be named "api" or "mobile" or "web"!
        ////////////////////////////////////////////////////////////////////////////////////////////////
        "/$controller/$action?/$id?(.$format)?"()

        ////////////////////////////////////////////////////////////////////////////////////////////////
        // System Pages without controller
        ////////////////////////////////////////////////////////////////////////////////////////////////
        "403"(view: '/_errors/403')
        "404"(view: '/_errors/404')
        "405"(view: '/_errors/error')
        "500"(view: '/_errors/error')
        "503"(view: '/_errors/503')

        ////////////////////////////////////////////////////////////////////////////////////////////////
        // wpt-server-proxy-mappings
        ////////////////////////////////////////////////////////////////////////////////////////////////

        "/proxy/$wptserver/getLocations.php" {
            controller = "WptProxy"
            action = [GET: "getLocations"]
        }

        "/proxy/$wptserver/runtest.php" {
            controller = "WptProxy"
            action = [POST: "runtest"]
        }

        "/proxy/$wptserver/results/$resultYear/$resultMonth/$resultDay/$resultFolder/$resultId/$fileToDownload" {
            controller = "WptProxy"
            action = [GET: "resultFileDownload"]
        }

        "/proxy/$wptserver/result/$resultId/" {
            controller = "WptProxy"
            action = [GET: "result"]
        }

        "/proxy/$wptserver/xmlResult/$resultId/" {
            controller = "WptProxy"
            action = [GET: "xmlResult"]
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////
        // rest api of osm
        ////////////////////////////////////////////////////////////////////////////////////////////////

        "/rest/man/$action?/$id?" {
            controller = "ApiDoc"
            action = "getDocuments"
        }

        // RawResultsApiController //////////////////////////////////////////

        "/rest/$system/resultsbetween/$timestampFrom/$timestampTo" {
            controller = "RawResultsApi"
            action = [GET: "getResults"]
        }

        "/rest/job/$id/resultUrls/$timestampFrom/$timestampTo" {
            controller = "RawResultsApi"
            action = [GET: "getResultUrls"]
        }

        "/rest/job/thresholdResult/$testId" {
            controller = "RawResultsApi"
            action = [GET: "getThresholdResults"]
        }

        "/rest/handleOldJobResults" {
            controller = "RawResultsApi"
            action = [PUT: "securedViaApiKeyHandleOldJobResults"]
        }

        // JobApiController //////////////////////////////////////////

        "/rest/job/$id/run" {
            controller = "JobApi"
            action = [POST: "runJob"]
        }

        "/rest/job/$id/activate" {
            controller = "JobApi"
            action = [PUT: "securedViaApiKeyActivateJob"]
        }
        "/rest/job/$id/deactivate" {
            controller = "JobApi"
            action = [PUT: "securedViaApiKeyDeactivateJob"]
        }
        "/rest/job/$id/setExecutionSchedule" {
            controller = "JobApi"
            action = [PUT: "securedViaApiKeySetExecutionSchedule"]
        }

        // CsiApiController //////////////////////////////////////////

        /* Since IT-248 */
        "/rest/$system/csi/$timestampFrom/$timestampTo" {
            controller = "CsiApi"
            action = [GET: "getEventResultBasedJobGroupCsi"]
        }

        /* Since IT-1007 */
        "/rest/$system/$page/csi/$timestampFrom/$timestampTo" {
            controller = "CsiApi"
            action = [GET: "getEventResultBasedPageCsi"]
        }

        "/rest/csi/translateToCustomerSatisfaction" {
            controller = "CsiApi"
            action = [GET: "translateToCustomerSatisfaction"]
        }

        /* Since IT-977 */
        "/rest/csi/csiConfiguration" {
            controller = "CsiApi"
            action = [GET: "getCsiConfiguration"]
        }

        // GeneralMeasurementApiController //////////////////////////////////////////

        "/rest/event/create" {
            controller = "GeneralMeasurementApi"
            action = [POST: "securedViaApiKeyCreateEvent"]
        }
        "/rest/config/activateMeasurementsGenerally" {
            controller = "GeneralMeasurementApi"
            action = [PUT: "securedViaApiKeyActivateMeasurement"]
        }
        "/rest/config/deactivateMeasurementsGenerally" {
            controller = "GeneralMeasurementApi"
            action = [PUT: "securedViaApiKeyDeactivateMeasurement"]
        }
        "/rest/config/activateNightlyDatabaseCleanup" {
            controller = "GeneralMeasurementApi"
            action = [PUT: "securedViaApiKeyActivateNightlyCleanup"]
        }
        "/rest/config/deactivateNightlyDatabaseCleanup" {
            controller = "GeneralMeasurementApi"
            action = [PUT: "securedViaApiKeyDeactivateNightlyCleanup"]
        }
        /* Since IT-81 */
        "/rest/allSystems" {
            controller = "GeneralMeasurementApi"
            action = [GET: "allSystems"]
        }
        /* Since IT-81 */
        "/rest/allBrowsers" {
            controller = "GeneralMeasurementApi"
            action = [GET: "allBrowsers"]
        }
        /* Since IT-81 */
        "/rest/allPages" {
            controller = "GeneralMeasurementApi"
            action = [GET: "allPages"]
        }
        /* Since IT-81 */
        "/rest/allLocations" {
            controller = "GeneralMeasurementApi"
            action = [GET: "allLocations"]
        }
        /* Since IT-81 */
        "/rest/allSteps" {
            controller = "InfrastructureApi"
            action = [GET: "allSteps"]
        }

        // DetailDataApiController //////////////////////////////////////////

        /* Since IT-1115 */
        "/rest/domain/idsForNames/$requestedDomains" {
            controller = "DetailDataApi"
            action = [GET: "getIdsForNames"]
        }
        /* Since IT-1115 */
        "/rest/domain/namesForIds/$requestedDomains" {
            controller = "DetailDataApi"
            action = [GET: "getNamesForIds"]
        }
        "/rest/receiveCallback" {
            controller = "DetailDataApi"
            action = [POST: "receiveCallback"]
        }

    }
}
