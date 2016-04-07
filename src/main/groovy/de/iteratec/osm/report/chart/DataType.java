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

package de.iteratec.osm.report.chart;

public enum DataType {

	TIME,COUNT,BYTE;
	
//	TIME,COUNT,BYTE;
//	
	public static boolean isYAxisLeft(DataType type) {
		if (type == DataType.TIME) return true;
		return false;
	}
	
	public static String getYAxisUnit(DataType type) {
		switch (type) {
		case TIME: 
			return "s";
		case COUNT: 
			return "";
		case BYTE:
			return "byte";
		}
		return "s";
	}
	
	public static String getYAxisLabel(DataType type) {
		switch (type) {
		case TIME: 
			return "Ausfuehrungszeit [s]";
		case COUNT: 
			return "Anzahl";
		case BYTE:
			return "Menge [byte]";
		}
		return "s";
	}
}
