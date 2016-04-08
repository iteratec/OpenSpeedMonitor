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

package de.iteratec.osm.report.external

import de.iteratec.osm.report.chart.AggregatorType

import org.grails.databinding.BindUsing

/**
 * A path configuration that defines the paths prefix and the measurand
 * for that a value is to be written to a {@link GraphiteServer}.
 *
 * @author nkuhn
 * @author mze
 *
 * @see MetricReportingService
 * @see AggregatorType
 */
class GraphitePath {
    // TODO mze-20131203: Rename to GraphitePathConfig
    @BindUsing({ obj, source -> source['prefix'] })
    String prefix
    AggregatorType measurand

    static constraints = {
        prefix(matches: /([a-zA-Z0-9]+\.)+/, nullable: false, blank: false, maxSize: 255)
        measurand(nullable: false)
    }

    @Override
    public String toString(){
        return "${prefix}[STATIC_PATH].${measurand.getName()}"
    }
}
