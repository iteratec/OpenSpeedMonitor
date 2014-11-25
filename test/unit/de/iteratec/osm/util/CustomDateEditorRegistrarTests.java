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

package de.iteratec.osm.util;

import java.beans.PropertyEditor;
import java.util.Date;

import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.beans.PropertyEditorRegistry;



/**
 * Test-suite of {@link CustomDateEditorRegistrar}.
 * 
 * @author mze
 * @since IT-74
 * 
 * @fixme FIXME MZE-2013-09-05: Move to package de.iteratec.isr.util.ui
 */
public class CustomDateEditorRegistrarTests {

	@Test
	public void testRegisterCustomEditors() {
		// Mock the registry:
		PropertyEditorRegistry registryMock = mock(PropertyEditorRegistry.class);

		// Test step 1: registry access:
		CustomDateEditorRegistrar out = new CustomDateEditorRegistrar();
		out.registerCustomEditors(registryMock);

		// Test step 2: Check what has been registered:
		ArgumentCaptor<PropertyEditor> argument = ArgumentCaptor
				.forClass(PropertyEditor.class);
		verify(registryMock).registerCustomEditor(eq(Date.class),
				argument.capture());
		
		PropertyEditor registeredEditor = argument.getValue();
		assertNotNull(registeredEditor);
		
		// Test step 3: Does the registered editor what is should:
		registeredEditor.setAsText("19.08.2013");
		
		Date converted = (Date) registeredEditor.getValue();
		
		// 1376863200000L => 19. August 2013, 0:00:00.0 CEST.
		assertEquals(1376863200000L, converted.getTime());
	}

}
