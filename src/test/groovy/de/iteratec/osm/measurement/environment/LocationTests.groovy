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

import org.junit.Assert;

import static org.junit.Assert.*;

import org.codehaus.groovy.grails.validation.ConstrainedProperty;
import org.codehaus.groovy.grails.validation.MaxSizeConstraint
import org.junit.Test;

import grails.test.mixin.Mock;
import grails.test.mixin.TestFor;

/**
 * Test-suite for {@link Location}
 */
@TestFor(Location)
@Mock(Location)
class LocationTests {

	@Test
	void testFindAll() {
		// Save one location
		new Location().save(failOnError: true, validate:false)

		// look for all -> current 1
		Assert.assertEquals(1, Location.all.size())

		// Save another location
		new Location().save(failOnError: true, validate:false)

		// look for all -> current 2
		Assert.assertEquals(2, Location.all.size())
	}

	@Test
	void testLabelSize() {
		// See
		//     http://grailssoapbox.blogspot.de/2010/02/accessing-grails-domain-constraints-at.html
		// and
		//     http://grailssoapbox.blogspot.de/2010/02/accessing-grails-domain-constraints-at.html
		// for documentation about constraint reflection.

		ConstrainedProperty labelConstraints = Location.constraints.label
		MaxSizeConstraint maxSizeConstraint = labelConstraints.getAppliedConstraint("maxSize")

		assertEquals("label increased from 50 to 150 on 30.7.2013", 150, maxSizeConstraint.getMaxSize())
	}
	
	@Test
	void testToString() {
		WebPageTestServer server = new WebPageTestServer(label: 'Server');
		Browser browser = new Browser(name: 'Browser');
		Location out = new Location(label: 'LocationLabel', location: 'Location', browser: browser, wptServer: server);
		
		assertEquals('Location @ Server (Browser)', out.toString());
	}
	
	@Test
	void testToString_realWorldData() {
		WebPageTestServer server = new WebPageTestServer(label: 'wpt1');
		Browser browser = new Browser(name: 'Firefox');
		Location out = new Location(label: 'Agent 1: Offizielles Monitoring', location: 'Agent1-wptdriver:Firefox7', browser: browser, wptServer: server);
		
		assertEquals('Agent1-wptdriver:Firefox7 @ wpt1 (Firefox)', out.toString());
	}
	
	@Test
	void testRemoveBrowser() {
		WebPageTestServer server = new WebPageTestServer(label: 'wpt1');
		Browser browser = new Browser(name: 'Chrome');
		Location loc1 = new Location(label: 'Agent 1: Offizielles Monitoring', location: 'Agent1-wptdriver:Firefox7', uniqueIdentifierForServer: 'ServerName02-Chrome', browser: browser, wptServer: server);
		Location loc2 = new Location(label: 'Agent 1: Offizielles Monitoring', location: 'Agent1-wptdriver:Firefox7', uniqueIdentifierForServer: 'Agent3-wptdriver:Chrome', browser: browser, wptServer: server);

		assertEquals('ServerName02', loc1.removeBrowser(loc1.uniqueIdentifierForServer))
		assertEquals('Agent3-wptdriver', loc2.removeBrowser(loc2.uniqueIdentifierForServer))		
	}
}
