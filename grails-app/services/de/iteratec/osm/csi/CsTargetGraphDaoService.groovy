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

import grails.gorm.transactions.Transactional
import de.iteratec.osm.util.I18nService


/**
 * Provides data access-methods for domain {@link CsTargetGraph}.
 * @author nkuhn
 *
 */
@Transactional
class CsTargetGraphDaoService {

	I18nService i18nService
	
	/**
	 * Delivers the customer satisfaction-target graph, which is valid, actually.
	 * <br><b>2014-01-14 Note:</b> Just one for the hole application for now.
	 * @return Customer satisfaction-target graph valid for the hole application.
	 * @throws NullPointerException if no {@link CsTargetGraph} exists with label equals i18n-entry with key de.iteratec.isocsi.targetcsi.label. 
	 */
    CsTargetGraph getActualCsTargetGraph() {
		CsTargetGraph actualCsTargetGraph = CsTargetGraph.findByLabel(i18nService.msg('de.iteratec.isocsi.targetcsi.label', 'Ziel-Kundenzufriedenheit'))
		Contract.requiresArgumentNotNull("Valid customer satisfaction-target graph", actualCsTargetGraph)
		return actualCsTargetGraph
    }
}
