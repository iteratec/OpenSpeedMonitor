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

package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.CsiConfiguration
import de.iteratec.osm.csi.CsiDay
import de.iteratec.osm.csi.TestDataUtil
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * Test-suite of {@link JobGroup}.
 */
@TestFor(JobGroup)
@Mock([JobGroup, CsiConfiguration, CsiDay])
class JobGroupTests {

	@Test
	public void testToString_CSI_group() {
		JobGroup out = new JobGroup(
				name: 'Test-Group-1');
		CsiConfiguration csiConfiguration = TestDataUtil.createCsiConfiguration()
		out.csiConfiguration = csiConfiguration

		assertEquals('Test-Group-1 (' + csiConfiguration.ident() + ')', out.toString());
	}

	@Test
	public void testToString_SYSTEM_group() {
		JobGroup out = new JobGroup(
				name: 'Test-Group-2');

		assertEquals('Test-Group-2', out.toString());
	}
}
