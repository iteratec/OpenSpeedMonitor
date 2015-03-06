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

package de.iteratec.osm.csi

/**
 * A Page of a web-application which performance is measured.
 *
 * @author nkuhn
 * @author mze
 */
class Page {

    /**
     * The name for an undefined page. Please use {@link #isUndefinedPage()}
     * to test if a page is undefined.
     */
    public static final String UNDEFINED = 'undefined'

    String name
    Double weight
    static final Double minWeight = 0

    static constraints = {
        name(unique: true, maxSize: 255)
        weight(min: this.minWeight)
    }

    static transients = ['undefinedPage']

    /**
     * <p>
     * Tests weather this page is an undefined, not specified page. This may
     * happen, if a result is not assigned or assignable to a page.
     * </p>
     *
     * @return <code>true</code> if and only if this page is undefined as
     *         described above, <code>false</code> else.
     *
     */
    public boolean isUndefinedPage() {
        return this.name.equals(Page.UNDEFINED)
    }

    String toString(){
        name
    }
}
