/*
 * OpenSpeedMonitor (OSM) Copyright 2014 iteratec GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package de.iteratec.osm.util;

import java.text.DecimalFormat;
import java.util.Locale;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.propertyeditors.CustomNumberEditor;

/**
 * <p>
 * A {@link PropertyEditorRegistrar} that registers a String-to-Double converter adjusting for the locale. Source:
 * http://stackoverflow.com/questions/14877602/grails-databinding-with-decimal-delimiter
 * </p>
 *
 * @author rkr
 * @since IT-505
 */

public class CustomDoubleRegistrar implements PropertyEditorRegistrar {

    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        // define new Double format with hardcoded Locale.ENGLISH
        registry.registerCustomEditor(Double.class, new CustomNumberEditor(Double.class, DecimalFormat.getInstance(Locale.ENGLISH), true));
    }
}
