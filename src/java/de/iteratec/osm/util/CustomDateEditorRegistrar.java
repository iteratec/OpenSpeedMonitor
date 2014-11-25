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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.propertyeditors.CustomDateEditor;

/**
 * <p>
 * A {@link PropertyEditorRegistrar} that registers a String-to-Date converter
 * for the common format used in openSpeed which is {@code dd.MM.yyyy}.
 * </p>
 * 
 * @author mze
 * @since IT-74
 */
public class CustomDateEditorRegistrar implements PropertyEditorRegistrar {

	@Override
	public void registerCustomEditors(PropertyEditorRegistry registry) {
		registry.registerCustomEditor(Date.class, createCustomDateEditor());
	}

	/**
	 * <p>
	 * Creates the editor for date registered by this registrar.
	 * </p>
	 * 
	 * @return not <code>null</code>; subsequent calls will always retrun a
	 *         newly created instance.
	 */
	public static CustomDateEditor createCustomDateEditor() {
		String dateFormat = "dd.MM.yyyy";
		return new CustomDateEditor(new SimpleDateFormat(dateFormat), true) {
			// For debugging purposes:
			
			@Override
			public Object getValue() {
				Object value = super.getValue();
				return value;
			}
			
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				super.setAsText(text);
			}
		};
	}
}
