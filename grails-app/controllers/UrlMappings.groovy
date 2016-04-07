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

/**
 * Base-URL-mappings for the application openSpeedMonitor.
 * @author nkuhn
 *
 */
class UrlMappings {

	static mappings = {

		/* EventResultDashboard as homepage */
		'/' (redirect: '/eventResultDashboard/showAll')

		/*
		 * Pages without controller
		 */
		"/releasenotes"		(view:"/siteinfo/releasenotes")
		"/about"		(view:"/siteinfo/about")
		"/systeminfo"	(view:"/siteinfo/systeminfo")

		/*
		 * Pages with controller
		 * WARN: No domain/controller should be named "api" or "mobile" or "web"!
		 */

		"/$controller/$action?/$id?(.$format)?"()

		/*
		 * For app-info-plugin
		 */
		"/admin/manage/$action?"(controller: "adminManage")
		"/adminManage/$action?"(controller: "errors", action: "urlMapping")

		/*
		 * System Pages without controller
		 */
		"403"	(view:'/_errors/403')
		"404"	(view:'/_errors/404')
		"500"	(view:'/_errors/error')
		"503"	(view:'/_errors/503')

		/*
		 * wpt-server-proxy-mappings
		 */

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

		/*
		 * rest api of osm
		 */

		"/rest/$system/resultsbetween/$timestampFrom/$timestampTo" {
			controller = "RestApi"
			action = [GET: "getResults"]
		}

		/* Since IT-81 */
		"/rest/man" {
			controller = "RestApi"
			action = [GET: "man"]
		}

		/* Since IT-81 */
		"/rest/allSystems" {
			controller = "RestApi"
			action = [GET: "allSystems"]
		}

		/* Since IT-81 */
		"/rest/allBrowsers" {
			controller = "RestApi"
			action = [GET: "allBrowsers"]
		}

		/* Since IT-81 */
		"/rest/allPages" {
			controller = "RestApi"
			action = [GET: "allPages"]
		}

		/* Since IT-81 */
		"/rest/allLocations" {
			controller = "RestApi"
			action = [GET: "allLocations"]
		}

		/* Since IT-81 */
		"/rest/allSteps" {
			controller = "RestApi"
			action = [GET: "allSteps"]
		}

		/* Since IT-248 */
		"/rest/$system/csi/$timestampFrom/$timestampTo" {
			controller = "RestApi"
			action = [GET: "getEventResultBasedCsi"]
		}

        /* Since IT-1007 */
        "/rest/$system/$page/csi/$timestampFrom/$timestampTo" {
            controller = "RestApi"
            action = [GET: "getEventResultBasedCsi"]
        }

		"/rest/csi/translateToCustomerSatisfaction" {
			controller = "RestApi"
			action = [GET: "translateToCustomerSatisfaction"]
		}
		"/rest/job/$id/resultUrls/$timestampFrom/$timestampTo" {
			controller = "RestApi"
			action = [GET: "getResultUrls"]
		}
		/* Since IT-977 */
		"/rest/csi/csiConfiguration" {
			controller = "RestApi"
			action = [GET: "getCsiConfiguration"]
		}

		/*
		 * Following PUT/POST rest api functions are secured via filter de.iteratec.osm.filters.SecureApiFunctionsFilters by
		 * naming convention of action methods.
		 */
		"/rest/job/$id/activate" {
			controller = "RestApi"
			action = [PUT: "securedViaApiKeyActivateJob"]
		}
		"/rest/job/$id/deactivate" {
			controller = "RestApi"
			action = [PUT: "securedViaApiKeyDeactivateJob"]
		}
        "/rest/job/$id/setExecutionSchedule" {
            controller = "RestApi"
            action = [PUT: "securedViaApiKeySetExecutionSchedule"]
        }
        "/rest/event/create" {
            controller = "RestApi"
            action = [POST: "securedViaApiKeyCreateEvent"]
        }
        "/rest/config/activateMeasurementsGenerally" {
            controller = "RestApi"
            action = [PUT: "securedViaApiKeySetMeasurementActivation"]
            activationToSet = true
        }
        "/rest/config/deactivateMeasurementsGenerally" {
            controller = "RestApi"
            action = [PUT: "securedViaApiKeySetMeasurementActivation"]
            activationToSet = false
        }
		"/rest/config/activateNightlyDatabaseCleanup" {
			controller = "RestApi"
			action = [PUT: "securedViaApiKeySetNightlyDatabaseCleanupActivation"]
			activationToSet = true
		}
		"/rest/config/deactivateNightlyDatabaseCleanup" {
			controller = "RestApi"
			action = [PUT: "securedViaApiKeySetNightlyDatabaseCleanupActivation"]
			activationToSet = false
		}

	}
}
