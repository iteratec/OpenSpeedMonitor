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

package de.iteratec.osm.measurement.environment

import de.iteratec.osm.measurement.environment.dao.BrowserDaoService

/**
 * Default implementation for {@link de.iteratec.osm.measurement.environment.dao.BrowserDaoService}.
 */
class DefaultBrowserDaoService implements BrowserDaoService {

	@Override
	public Set<Browser> findAll() {
		Set<Browser> result = Collections.checkedSet(new HashSet<Browser>(), Browser.class);
		result.addAll(Browser.list());
		return Collections.unmodifiableSet(result);
	}
	
	@Override
	Browser tryToFindByNameOrAlias(String browserNameOrAlias) {
		Browser ret = Browser.findByName(browserNameOrAlias)
		if (ret==null) {
			return findByAlias(browserNameOrAlias)
		}else{
			return ret
		}
	}
	
	private Browser findByAlias(browserNameOrAlias){
		Browser ret = Browser.findByName('undefined')?:new Browser(name:'undefined').save(failOnError:true)
		Browser.list().each{Browser currBrowser ->
			def query = BrowserAlias.where{
				browser == currBrowser
			}
			query.findAll{it.alias.equals(browserNameOrAlias)}.each{
				ret = currBrowser
			}
		}
		return ret
	}
}
