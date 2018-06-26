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

import grails.databinding.BindUsing
import groovy.transform.EqualsAndHashCode

import java.text.SimpleDateFormat


/**
 * One 2-dimensional point of two representing a {@link CsTargetGraph}.
 * @author nkuhn
 *
 */
@EqualsAndHashCode
class CsTargetValue {

    private final static String DATE_FORMAT_STRING = 'yyyy-MM-dd';

    @BindUsing({
        obj, source ->
            def dateObject = source['date']
            Date date = null
            if(dateObject instanceof Date){
                date = dateObject
            } else{
                String dateString = dateObject
                if(dateString) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING)
                    date = dateFormat.parse(dateString)
                }
            }
            return date
    })
    Date date
    Double csInPercent
    static final Double minCsInPercent = 0
    static final Double maxCsInPercent = 100

    static constraints = {
        date(nullable: false)
        csInPercent(min: this.minCsInPercent, max: this.maxCsInPercent)
    }
    public String toString(){
        return "${date} | ${csInPercent}"
    }
}
