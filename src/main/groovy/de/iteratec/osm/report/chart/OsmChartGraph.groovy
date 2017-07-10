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

package de.iteratec.osm.report.chart

import de.iteratec.osm.result.MeasurandGroup

/**
 * Single graph to show in a highchart-diagram.
 * @author nkuhn
 *
 */
class OsmChartGraph {
	/**
	 * Label to be shown in legend of diagram.
	 */
	String label
	/**
	 * {@link de.iteratec.osm.result.MeasurandGroup} of the graph. Depending on this, the graph will be associated to a Y-achsis.
	 */
	MeasurandGroup measurandGroup
	/**
	 * The list of {@link OsmChartPoint}s this graph consists of.
	 */
	List<OsmChartPoint> points = []
	
	
	public Double getMaxCsiAggregation()
	{
		if (points == null || points.size() <= 0)
		{
			return null;
		}
		
		Double max = Double.MIN_VALUE
		for (OsmChartPoint p : points)
		{
			if (p.csiAggregation > max)
			{
				max = p.csiAggregation
			}
		}	
		
		return max
	}
	
	public Double getMinCsiAggregation()
	{
		if (points == null || points.size() <= 0)
		{
			return null;
		}
		
		Double min = Double.MAX_VALUE
		for (OsmChartPoint p : points)
		{
			if (p.csiAggregation < min)
			{
				min = p.csiAggregation
			}
		}
		
		return min
	}
	
	/*-
	 * Architecture note on "init":
	 * 
	 * This class could have a constructor and the fields could be final.
	 * Doing this restrictions of the field values would be check at this
	 * centralized location.
	 * 
	 * mze-2013-10-15
	 */
	
	/*-
	 * Architecture note on "points":
	 * 
	 * The list points should only be modified by this object itself. This 
	 * would centralize the kind of sorting and the restrictions for adding.
	 * The list should be visible as unmodifiable version only 
	 * (override getter and return a unmodifiable view). 
	 * 
	 * The modification depends on this class structure and the structure 
	 * should be a part of the class' secrets (information hiding).
	 * 
	 * Modifications could be achieved by a dedicated method like:
	 *     addGraph(String label, List<Points>)
	 * (the point could be passed as Iterable and then copied here is 
	 *  a sorted version (List) if this should also be part of secret)
	 * 
	 * mze-2013-10-15
	 */
}
