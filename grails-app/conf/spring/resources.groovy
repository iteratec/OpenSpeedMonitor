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


import de.iteratec.osm.measurement.environment.DefaultBrowserDaoService
import de.iteratec.osm.measurement.environment.DefaultLocationDaoService
import de.iteratec.osm.measurement.schedule.DefaultJobGroupDaoService
import de.iteratec.osm.measurement.schedule.DefaultPageDaoService
import de.iteratec.osm.report.chart.DefaultAggregatorTypeDaoService
import de.iteratec.osm.report.external.provider.DefaultGraphiteSocketProvider
import de.iteratec.osm.result.dao.DefaultMeasuredEventDaoService


// Place your Spring DSL code here

beans = {
    aggregatorTypeDaoService(DefaultAggregatorTypeDaoService)
    jobGroupDaoService(DefaultJobGroupDaoService)
    pageDaoService(DefaultPageDaoService)
    measuredEventDaoService(DefaultMeasuredEventDaoService)
    browserDaoService(DefaultBrowserDaoService)
    locationDaoService(DefaultLocationDaoService)
	graphiteSocketProvider(DefaultGraphiteSocketProvider)
}
