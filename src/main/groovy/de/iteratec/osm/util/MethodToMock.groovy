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

package de.iteratec.osm.util

import java.lang.reflect.Method

/**
 *	Defines a method that should be mocked in a grails service and the return value that should be returned from mocked method.
 * @author nkuhn on 13.01.15.
 * @see ServiceMocker
 */
class MethodToMock {
	/** Method to be mocked. */
    Method method
	/** Value to be returned from mocked method. */
    def toReturn
}
