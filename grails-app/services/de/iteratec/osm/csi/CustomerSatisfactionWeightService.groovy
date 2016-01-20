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

import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.util.I18nService

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

    I18nService i18nService
    private Map<Integer, Double> hoursOfDay

    /**
     * Validates uploaded csv with weights for csi regarding format.
     * @param weightFactor
     * @param csv
     * @return
     */
    List<String> validateWeightCsv(WeightFactor weightFactor, InputStream csv) {
        List<String> errorMessages = []
        List<Class> classes = new ArrayList<>()
        List<String> lines = new ArrayList<>()

        csv.eachLine { line -> lines.add(line) }

        switch (weightFactor) {
            case WeightFactor.HOUROFDAY:
                errorMessages.addAll(validateHourofDay(lines))
            case WeightFactor.BROWSER:
            case WeightFactor.PAGE:
                classes.add(String)
                classes.add(Double)
                break
            case WeightFactor.BROWSER_CONNECTIVITY_COMBINATION:
                errorMessages.addAll(validateBrowserAndConnectivtiyExisting(lines))
                classes.add(String)
                classes.add(String)
                classes.add(Double)
                break
        }
        errorMessages.addAll(validateCsvFormat(classes, lines))

        return errorMessages
    }

    private List<String> validateCsvFormat(List<Class> classes, List<String> lines) {
        List<String> errorMessages = []

        int columnCount = classes.size()
        int rowCounter = 0
        int incorrectLine = 0

        lines.each { line ->
            if (rowCounter == 0) {
                if (!headerLineCorrect(line, columnCount))
                    errorMessages.add(i18nService.msg("de.iteratec.osm.csi.csvErrors.header", "Falscher Header", [columnCount]))
            } else {
                if (!lineCorrect(line, columnCount, classes)) {
                    incorrectLine++;
                }
            }
            ++rowCounter
        }

        if (incorrectLine > 0) {
            String csvFormat = "[" + classes*.getSimpleName().join(";") + "]"
            errorMessages.add(i18nService.msg("de.iteratec.osm.csi.csvErrors.incorrectLine", "incorrect Line in csv", [csvFormat]))
        }

        return errorMessages
    }

    private boolean lineCorrect(String line, int columnCount, List<Class> classes) {
        boolean correct = true
        List tokenized = line.tokenize(";")


        for (int i = 0; i < classes.size(); i++) {
            if (classes[i] == String.class) {
                if (!Pattern.matches('.+', tokenized[i])) {
                    correct = false
                }
            } else if (classes[i] == Double.class || classes[i] == Float.class) {
                if (tokenized[i] && !tokenized[i].isNumber()) {
                    correct = false
                }
            } else {
                log.error("Validation for class " + classes[i] + " not implemented yet")
            }
        }

        return correct
    }

    private boolean headerLineCorrect(String headerLine, int columnCount) {
        StringBuilder builder = new StringBuilder()
        builder.append("[a-zA-Z]+")
        (columnCount - 1).times {
            builder.append(";[a-zA-Z]+")
        }

        String pattern = builder.toString()
        Pattern.matches(pattern, headerLine)
    }

    private List<String> validateHourofDay(List<String> lines) {
        List<String> errorMessages = []
        int lineCounter = 0
        List<Integer> hoursOfDay = []
        lines.each { line ->
            if (lineCounter != 0) {
                def tokenized = line.tokenize(';')
                tokenized[0].isInteger() ? hoursOfDay.add(new Integer(tokenized[0])) : hoursOfDay.add(-1)
            }
            lineCounter++
        }

        if (hoursOfDay.contains(-1) || hoursOfDay.size() != 24) {
            errorMessages.add(i18nService.msg("de.iteratec.osm.csi.csvErrors.hourOfDay", "nicht alle 24 Stunden berücksichtigt"))
        }

        return errorMessages
    }

    private List<String> validateBrowserAndConnectivtiyExisting(List<String> lines) {
        List<String> errorMessages = []

        // exclude header row
        for (int i = 1; i < lines.size(); ++i) {
            List<String> tokenize = lines[i].tokenize(';')

            Browser browser = Browser.findByName(tokenize[0])
            ConnectivityProfile connectivityProfile = ConnectivityProfile.findByName(tokenize[1])

            if (browser == null) {
                errorMessages.add(i18nService.msg("de.iteratec.osm.csi.csvErrors.browserDoesNotExist", "browser nicht vorhanden", [tokenize[0]]))
            }
            if (connectivityProfile == null) {
                errorMessages.add(i18nService.msg("de.iteratec.osm.csi.csvErrors.connectivityDoesNotExist", "verbindung nicht vorhanden", [tokenize[1]]))
            }
        }

        return errorMessages
    }

    /**
     * Persists uploaded csv with weights for csi.
     * @param weightFactor
     * @param csv
     * @return
     */
    def persistNewWeights(WeightFactor weightFactor, InputStream csv) {
        switch (weightFactor) {
            case WeightFactor.BROWSER: persistBrowserWeights(csv); break
            case WeightFactor.PAGE: persistPageWeights(csv); break
            case WeightFactor.HOUROFDAY: persistHourofdayWeights(csv); break
            case WeightFactor.BROWSER_CONNECTIVITY_COMBINATION: persistBrowserConnectivityWeights(csv); break
        }
    }

    private persistBrowserWeights(InputStream csv) {
        Integer lineCounter = 0
        csv.eachLine { line ->
            if (lineCounter > 0) {
                List tokenized = line.tokenize(";")
                Browser browser = Browser.findByName(tokenized[0])
                if (browser) {
                    log.info("update browser-weight: name=${tokenized[0]}, weight=${tokenized[1]}")
                    browser.weight = Double.valueOf(tokenized[1])
                    browser.save(failOnError: true)
                } else {
                    log.info("save new browser-weight: name=${tokenized[0]}, weight=${tokenized[1]}")
                    new Browser(name: tokenized[0], weight: Double.valueOf(tokenized[1])).save(failOnError: true)
                }
            }
            lineCounter++
        }
    }

    private persistBrowserConnectivityWeights(InputStream csv) {
        Integer lineCounter = 0
        csv.eachLine { line ->
            if (lineCounter > 0) {
                List tokenized = line.tokenize(";")
                Browser browser = Browser.findByName(tokenized[0])
                ConnectivityProfile connectivityProfile = ConnectivityProfile.findByName(tokenized[1])

                BrowserConnectivityWeight browserConnectivityWeight = BrowserConnectivityWeight.findByBrowserAndConnectivity(browser, connectivityProfile)

                if (tokenized[2]) {
                    double newWeight = Double.parseDouble(tokenized[2])
                    if (browserConnectivityWeight) {
                        log.info("update browser-connectivity-weight: browser=${tokenized[0]}, connectivity=${tokenized[1]}, weight=${newWeight}")
                        browserConnectivityWeight.weight = Double.valueOf(newWeight)
                        browser.save(failOnError: true)
                    } else {
                        log.info("save new browser-weight: browser=${tokenized[0]}, connectivity=${tokenized[1]}, weight=${newWeight}")
                        new BrowserConnectivityWeight(browser: browser, connectivity: connectivityProfile, weight: Double.valueOf(newWeight)).save(failOnError: true)
                    }
                } else {
                    if (browserConnectivityWeight) {
                        browserConnectivityWeight.delete(flush: true)
                    }
                }
            }
            lineCounter++
        }
    }

    private persistPageWeights(InputStream csv) {
        Integer lineCounter = 0
        csv.eachLine { line ->
            if (lineCounter > 0) {
                List tokenized = line.tokenize(";")
                Page page = Page.findByName(tokenized[0])
                if (page) {
                    log.info("update Page-weight: name=${tokenized[0]}, weight=${tokenized[1]}")
                    page.weight = Double.valueOf(tokenized[1])
                    page.save(failOnError: true)
                } else {
                    log.info("save new Page-weight: name=${tokenized[0]}, weight=${tokenized[1]}")
                    new Page(name: tokenized[0], weight: Double.valueOf(tokenized[1])).save(failOnError: true)
                }
            }
            lineCounter++
        }
    }

    private persistHourofdayWeights(InputStream csv) {
        Integer lineCounter = 0
        csv.eachLine { line ->
            if (lineCounter > 0) {
                List tokenized = line.tokenize(";")
                HourOfDay hourOfDay = HourOfDay.findByFullHour(tokenized[0])
                if (hourOfDay) {
                    log.info("update Hourofday-weight: name=${tokenized[0]}, weight=${tokenized[1]}")
                    hourOfDay.weight = Double.valueOf(tokenized[1])
                    hourOfDay.save(failOnError: true)
                } else {
                    log.info("save new Hourofday-weight: name=${tokenized[0]}, weight=${tokenized[1]}")
                    new HourOfDay(fullHour: tokenized[0], weight: Double.valueOf(tokenized[1])).save(failOnError: true)
                }
            }
            lineCounter++
        }
    }

    void persistNewDefaultMapping(InputStream csv) {
        Integer lineCounter = 0
        csv.eachLine {line ->
            if (lineCounter > 0) {
                List tokenized = line.tokenize(";")
                String name = tokenized[0]
                int loadTimeInMs = Integer.parseInt(tokenized[1])
                double customerSatisfation = Double.parseDouble(tokenized[2])
                DefaultTimeToCsMapping mapping = DefaultTimeToCsMapping.findByNameAndLoadTimeInMilliSecs(name, loadTimeInMs)
                if(mapping) {
                    mapping.customerSatisfactionInPercent = customerSatisfation
                    mapping.save(failOnError: true)
                } else {
                    new DefaultTimeToCsMapping(name: name, loadTimeInMilliSecs: loadTimeInMs, customerSatisfactionInPercent: customerSatisfation).save(failOnError: true)
                }
            }
            lineCounter++;
        }
    }
}
