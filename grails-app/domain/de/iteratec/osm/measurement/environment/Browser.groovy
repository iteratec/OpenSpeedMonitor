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

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'name')
class Browser {

    /**
     * The name for an undefined page. Please use {@link #isUndefinedPage()}
     * to test if a page is undefined.
     */
    public static final String UNDEFINED = 'undefined'

    String name

    static hasMany = [browserAliases: BrowserAlias]

    static constraints = {
        name(unique:true, maxSize: 255)
    }

    static transients = ['undefinedBrowser']

    /**
     * <p>
     * Tests weather this browser is an undefined, not specified browser. This happens, if the first result
     * of a new {@link Job} comes in. The browser has to be assigned afterwards, manually.
     * </p>
     *
     * @return <code>true</code> if and only if this browser is undefined as
     *         described above, <code>false</code> else.
     *
     */
    public boolean isUndefinedBrowser() {
        return this.name.equals(Browser.UNDEFINED)
    }

    @Override
    public String toString(){
        return "${name}"
    }
}
