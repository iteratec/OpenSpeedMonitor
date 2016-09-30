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

package de.iteratec.osm.result;

public enum WptXmlResultVersion {
    /**
     * XML-result measured with a webpagetest-server/-agent without multistep-capabilities.
     */
    BEFORE_MULTISTEP,
    /**
     * XML-result measured with a webpagetest-server/-agent with forked multistep-adaption of iteratec.
     */
    MULTISTEP_FORK_ITERATEC,
    /**
     * XML-result measured with a multistep capable webpagetest-server/-agent > version 2.19.
     * Multistep fork of iteratec was merged into main code with version 2.19.
     */
    MULTISTEP
}
