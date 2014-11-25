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
 * PLEASE NEVER COMMIT CHANGED VERSIONS OF THIS FILE.
 * 
 * The datasources defined in this file are default datasources to be used only when running the app out of the box via run-app.
 * Datasources different from default can and should be defined in separate external config files. 
 * Config param grails.config.locations in Config.groovy contains a list of possible locations for such additional config files.
 * In addition you can add an own location for an external config file via system property "${appName}.config.location"
 * 
 * @author nkuhn
 * @see OpenSpeedMonitor-config.groovy.sample
 * @see Config.groovy
 * @see http://grails.org/doc/latest/guide/conf.html#configExternalized
 *
 */
// general settings
dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
	cache.provider_class='org.hibernate.cache.EhCacheProvider'
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
		dataSource {
            dbCreate = "update"// one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    test {
        dataSource {
            dbCreate = "update"// one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    production {
			dataSource {
            dbCreate = "update"// one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
}
