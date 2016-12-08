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

package de.iteratec.osm.report.ui

class OptGroupedSelectTagLib {
	
	static namespace = "iteratec"
	
	def optGroupedSelect = { attrs -> 
		Map dataMap = attrs['dataMap']
		String optionKey = attrs['optionKey']
		String optionValue = attrs['optionValue']
		String multiple = attrs['multiple'] ? "multiple" : ""
		String id = attrs['id']
		String cssClass = attrs['class']
		String style = attrs['style']
		String name = attrs['name']
		String value = attrs['value']
		out << g.render(template: '/layouts/optGroupedSelect', 
			model: [dataMap:dataMap, optionKey: optionKey, optionValue: optionValue, multiple: multiple, id: id, cssClass: cssClass, style: style,
				name: name, selectedValues: value])
	}

	def releaseNotes = { attrs ->
		String text = attrs['text']

		out << text.replaceAll('\n', '<br>')
	}
}
