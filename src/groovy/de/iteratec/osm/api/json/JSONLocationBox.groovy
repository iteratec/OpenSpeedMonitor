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

package de.iteratec.osm.api.json

/**
 * <p>
 * Using this box gives a simple string the key 'location' instead of
 * the JSON default 'value' which makes results where a location was 
 * expected more readable. 
 * </p>
 * 
 * @author mze
 * @since IT-81
 */
public final class JSONLocationBox {
	public final String location;
	
	public JSONLocationBox(String location)
	{
		this.location = location;
	}

	@Override
	public String toString() {
		return "JSONLocationBox [location=" + location + "]";
	}
	
}
