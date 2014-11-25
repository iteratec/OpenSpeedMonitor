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

package de.iteratec.osm.filters

/**
 * PerformanceFilterFilters
 * A filters class is used to execute code before and after a controller action is executed and also after a view is rendered
 */
class PerformanceFilters {

	static ThreadLocal<Long> actionExecutionTime = new ThreadLocal<Long>(){
		@Override
		protected Long initialValue() {
			return new Long(0);
		}
	};
	static ThreadLocal<Long> viewExecutionTime = new ThreadLocal<Long>(){
		@Override
		protected Long initialValue() {
			return new Long(0);
		}
	};

	def filters = {
		/* the following filter measures server-side execution time from entering a controller to rendering the corresponding view
		 */
//		justJob(controller:'job', action:'*') {
//			before = {
//				long currentTime = System.currentTimeMillis();
//				actionExecutionTime.set( currentTime  );
//			}
//			after = { Map model ->
//				long currentTime = System.currentTimeMillis();
//				actionExecutionTime.set( currentTime - actionExecutionTime.get() );
//				viewExecutionTime.set( currentTime );
//			}
//			afterView = { Exception e ->
//				long currentTime = System.currentTimeMillis();
//				viewExecutionTime.set( currentTime - viewExecutionTime.get() );
//				
//				println '***************';
//				println 'Action named "' + actionName + '" of controller "' + controllerName + '"';
//				println ' - action takes : ' + actionExecutionTime.get() + ' ms';
//				println ' - view takes   : ' + viewExecutionTime.get() + ' ms';
//				println '***************';
//			}
//		}
	}
}
