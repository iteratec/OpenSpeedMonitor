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

import java.util.regex.Pattern

import de.iteratec.osm.csi.weighting.WeightFactor
import de.iteratec.osm.measurement.environment.Browser

/**
 * Contains logic for persistence and validation of mappings for {@link WeightFactor}s.
 * New Mappings can be imported in to the system in form of csv-files. 
 * 
 * @author nkuhn
 *
 */
class CustomerSatisfactionWeightService {
	
	private Map<Integer, Double> hoursOfDay

	/**
	 * Validates uploaded csv with weights for csi regarding format.
	 * @param weightFactor
	 * @param csv
	 * @return
	 */
    def List<String> validateWeightCsv(WeightFactor weightFactor, InputStream csv) {
		log.info("validation of csv-file for ${weightFactor.toString()}")
		List<String> errorMessages = []
		
		Integer rowCounter=0
		Integer incorrectLines = 0
		Set<Integer> hoursOfDay = new HashSet<Integer>()
		csv.eachLine{
			log.info("row ${rowCounter} of validated csv=$it")
			if (rowCounter==0) {
				validateHeaderLineOfWeightCsv(it, errorMessages)
			}else{
				if (validateFormatOfWeightcsvline(it, weightFactor, hoursOfDay)==false) {
					incorrectLines++
				}
			}
			rowCounter++
		}
		if (weightFactor==WeightFactor.HOUROFDAY) {
			if (hoursOfDay.contains(-1) || hoursOfDay.size()!=24) {
				errorMessages.add("Die CSV-Datei enthält nicht für alle 24 Stunden des Tages eine Datenzeile!")
			}
		}
		if (incorrectLines>0) {
			errorMessages.add("Nicht alle Datenzeilen der CSV-Datei entsprechen dem Format [String;Double]!\n"+
				"Zahlenwerte bitte im amerikanischen Format eingeben.")
		}
		return errorMessages
    }
	
	/**
	 * Persists uploaded csv with weights for csi.
	 * @param weightFactor
	 * @param csv
	 * @return
	 */
	def persistNewWeights(WeightFactor weightFactor, InputStream csv){
		switch (weightFactor){
			case WeightFactor.BROWSER : persistBrowserWeights(csv) ; break
			case WeightFactor.PAGE : persistPageWeights(csv) ; break
			case WeightFactor.HOUROFDAY : persistHourofdayWeights(csv) ; break
		}
	}
	
	/**
	 * Provides lazy loaded map of weighted hours of days.
	 * @return
	 */
	Map<Integer, Double> getHoursOfDay(){
		if (hoursOfDay==null) {
			hoursOfDay = [:]
			HourOfDay.findAll().each{
				hoursOfDay[it.fullHour] = it.weight
			}	
		}
		return hoursOfDay
	}
	
	private persistBrowserWeights(InputStream csv){
		Integer lineCounter=0
		csv.eachLine{line ->
			if (lineCounter>0) {
				List tokenized = line.tokenize(";")
				Browser browser = Browser.findByName(tokenized[0])
				if (browser) {
					log.info("update browser-weight: name=${tokenized[0]}, weight=${tokenized[1]}")
					browser.weight=Double.valueOf(tokenized[1])
					browser.save(failOnError: true)
				} else {
					log.info("save new browser-weight: name=${tokenized[0]}, weight=${tokenized[1]}")
					new Browser(name: tokenized[0], weight: Double.valueOf(tokenized[1])).save(failOnError: true)
				}
			}
			lineCounter++
		}
	}
	private persistPageWeights(InputStream csv){
		Integer lineCounter=0
		csv.eachLine{line ->
			if (lineCounter>0) {
				List tokenized = line.tokenize(";")
				Page page = Page.findByName(tokenized[0])
				if (page) {
					log.info("update Page-weight: name=${tokenized[0]}, weight=${tokenized[1]}")
					page.weight=Double.valueOf(tokenized[1])
					page.save(failOnError: true)
				} else {
					log.info("save new Page-weight: name=${tokenized[0]}, weight=${tokenized[1]}")
					new Page(name: tokenized[0], weight: Double.valueOf(tokenized[1])).save(failOnError: true)
				}
			}
			lineCounter++
		}
	}
	private persistHourofdayWeights(InputStream csv){
		Integer lineCounter=0
		csv.eachLine{line ->
			if (lineCounter>0) {
				List tokenized = line.tokenize(";")
				HourOfDay hourOfDay = HourOfDay.findByFullHour(tokenized[0])
				if (hourOfDay) {
					log.info("update Hourofday-weight: name=${tokenized[0]}, weight=${tokenized[1]}")
					hourOfDay.weight=Double.valueOf(tokenized[1])
					hourOfDay.save(failOnError: true)
				} else {
					log.info("save new Hourofday-weight: name=${tokenized[0]}, weight=${tokenized[1]}")
					new HourOfDay(fullHour: tokenized[0], weight: Double.valueOf(tokenized[1])).save(failOnError: true)
				}
			}
			lineCounter++
		}
	}
	
	private validateHeaderLineOfWeightCsv(String headerLine, List<String> errorMessages){
		if(!Pattern.matches("[a-zA-Z]+;[a-zA-Z]+", headerLine)){
			errorMessages.add("Header-Zeile enthält weniger oder mehr als 2 Spaltenüberschriften!")
		}
	}
	private validateFormatOfWeightcsvline(String line, WeightFactor weightCategory, Set<Integer> hoursOfDay){
		List tokenized = line.tokenize(";")
		Boolean correctFormat = true
		if  (tokenized.size()!=2 || !Pattern.matches('.+', tokenized[0]) || !tokenized[1].isNumber()) {
			correctFormat = false
		}else{
			if (weightCategory==WeightFactor.HOUROFDAY) {
				tokenized[0].isInteger()?hoursOfDay.add(new Integer(tokenized[0])):hoursOfDay.add(-1)
			}
		}
		return correctFormat
	}
	
	
	private validateHourofDay(String line, Set<Integer> hoursOfDay){
		List tokenized = line.tokenize(";")
		if(!Pattern.matches("[a-zA-Z]+;[a-zA-Z]+", headerLine)){
			errorMessages.add("Header-Zeile enthält weniger oder mehr als 2 Spaltenüberschriften!")
		}
	}
	
}
