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
 * Grails-dependent quartz-config (for quartz-plugin, specifically)
 */
quartz {
    autoStartup = true
    jdbcStore = false
    waitForJobsToCompleteOnShutdown = true
    exposeSchedulerInRepository = false

    props {
        scheduler.skipUpdateCheck = true
    }
}

environments {
    test {
        quartz {
            autoStartup = false
        }
    }
}/**
 * grails-independent quartz-config (irrelevant if jdbcStore is set to false)
 */
org.quartz{
	// Configure ThreadPool ///////////////////////////////////////////////////////////////////////////////
	threadPool.class = simpl.SimpleThreadPool
	threadPool{
		threadCount = 25
		threadPriority = 5
	}
	
	// Configure JobStore ///////////////////////////////////////////////////////////////////////////////
	// From quartz-docs:
	// If you don't need to tie your scheduling commands (such as adding and removing triggers) to other transactions, then you can let Quartz manage the transaction
	// by using JobStoreTX as your JobStore (this is the most common selection).
	jobStore.class = impl.jdbcjobstore.JobStoreTX
	// From quartz-docs:
	// If you need Quartz to work along with other transactions (i.e. within a J2EE application server), then you should use JobStoreCMT - in which case Quartz
	// will let the app server container manage the transactions.
	//jobStore.class = impl.jdbcjobstore.JobStoreCMT
	jobStore{
		driverDelegateClass = impl.jdbcjobstore.StdJDBCDelegate
		// From quartz-docs:
		// The "jobStore.useProperties" config parameter can be set to "true" (defaults to false) in order to instruct JDBCJobStore that all values in
		// JobDataMaps will be Strings, and therefore can be stored as name-value pairs, rather than storing more complex objects in their serialized form in the BLOB column.
		// This is much safer in the long term, as you avoid the class versioning issues that there are with serializing your non-String classes into a BLOB.
		useProperties = false
		dataSource = 'osmDS'
		tablePrefix = 'QRTZ_'
	}
	
	// Configure Datasources ///////////////////////////////////////////////////////////////////////////////
	dataSource{
		osmDS.driver = '[jdbc driver class]'
		osmDS.URL = '[jdbc conn string]'
		osmDS.user = '[osm_db_user]'
		osmDS.password = '[osm_db_password]'
		osmDS.maxConnections = 5
		osmDS.validationQuery='SELECT 1'
	}
}