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
/**
 * <p>
 * This class defines the abstraction of a highchart lineType used in the JavaScript lib and {@link OsmChartTagLib}.
 * </p>
 * 
 * @author rhe
 * @since IT-132
 * 
 */
public class OsmChartAxis {
	public static final int LEFT_CHART_SIDE=0;
	public static final int RIGHT_CHART_SIDE=1;
	
	static mapWith = "none";
	
	MeasurandGroup group;
	
	String labelI18NIdentifier;
	String unit;
	
	String color;
	
	Double divisor;	
	
	/**
	 * XXX mze-2013-10-01: Warum ist das ein int? Hat das Vorteile für die 
	 * Highchart-lib? Sonst hätte ich hier ein Enum erwartet 
	 * -> Vorteil: Keine invaliden Werte möglich.
	 * Bei der Gelegenheit könnte man alle properties final (read-only) machen. 
	 */
	Integer labelPosition;
	
	/**
	 * Constructs a HighChartLabel for a {@link OsmChartPoint}-collection.
	 * 
	 * @param i18NIdentifier the i18n-Identifier, not <code>null</code>
	 * @param group the {@link MeasurandGroup}, not <code>null</code>
	 * @param unit a string to display its unit-type, not <code>null</code>
	 * @param color a color string in hex-notation (#xxxxxx), not <code>null</code>
	 * @param divisor a divisor the results above should be divided by, not <code>null</code> and not <code>0</code>
	 * @param labelPosition
	 */
	public OsmChartAxis(String i18NIdentifier, MeasurandGroup group, String unit, String color, Double divisor, int labelPosition) {
		if(divisor==0) {
			throw new IllegalArgumentException("Can not divide by 0");
		}
		
		this.group=group;
		this.labelI18NIdentifier=i18NIdentifier;
		this.labelPosition=labelPosition;
		this.divisor=divisor;
		this.unit=unit;
		this.color=color;
	}
	
	/**
	 * Constructs a HighChartLabel for a {@link OsmChartPoint}-collection.
	 * 
	 * <p>
	 * The default color is #000000;
	 * </p>
	 *
	 * @param i18NIdentifier the i18n-Identifier, not <code>null</code>
	 * @param group the {@link MeasurandGroup}, not <code>null</code>
	 * @param unit a string to display its unit-type, not <code>null</code>
	 * @param divisor a divisor the results above should be divided by, not <code>null</code> and not <code>0</code>
	 * @param labelPosition
	 */
	public OsmChartAxis(String i18NIdentifier, MeasurandGroup group, String unit, Double divisor, int labelPosition) {
		this(i18NIdentifier, group, unit, "#000000", divisor, labelPosition);
	}
		
}
