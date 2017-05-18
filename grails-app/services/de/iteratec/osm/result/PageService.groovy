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

package de.iteratec.osm.result

import org.joda.time.DateTime
import org.joda.time.Duration

import de.iteratec.osm.csi.Page

/**
 * DB-access and caching for {@link Page}s.
 * @author nkuhn
 *
 */
class PageService {

    /** Stepname can contain information of tested {@link Page} and teststep-number. Both informations are delimitted through this. */
    public static final String STEPNAME_DELIMITTER = ':::'
    /** Stepname can contain information of tested {@link Page} and teststep-number. Steps measured with an older wpt-version (without multistep-functionality) have this as teststep-number. */
    public static final String STEPNAME_DEFAULT_STEPNUMBER = 'Step01'

    /**
     * Gets Page from db according to given stepName.
     * StepName: [pagename]STEPNAME_DELIMITTER[restOfStepName]
     * @param stepName
     * @return
     */
    Page getPageByStepName(String stepName) {
        Page page = Page.findByName(getPageNameFromStepName(stepName))
        return page ? page : Page.findByName(Page.UNDEFINED)
    }
    /**
     * Gets pagename-part from given stepName. If no pagename-part exist {@link Page#UNDEFINED} is returned.
     * @param stepName
     * @return
     * @see {@link #STEPNAME_DELIMITTER}
     */
    String getPageNameFromStepName(String stepName) {
        List<String> tokenized = stepName.split(STEPNAME_DELIMITTER)
        return tokenized.size() == 2 ? tokenized[0] : Page.UNDEFINED
    }
    /**
     * Cuts Pagename-part from start of stepname if it's delimitted by {@link #STEPNAME_DELIMITTER}.
     * @param stepName
     * 			Name of a {@link MeasuredEvent}.
     * @return stepName excluded Pagename-part  if it's delimitted by {@link #STEPNAME_DELIMITTER}. Full and unchanged stepName if it's not.
     *
     */
    String excludePagenamePart(String stepName) {
        return stepName.contains(STEPNAME_DELIMITTER) ?
                stepName.substring(stepName.indexOf(STEPNAME_DELIMITTER) + STEPNAME_DELIMITTER.length(), stepName.length()) :
                stepName
    }
    /**
     * Stepname can contain information of tested {@link Page} and teststep-number.
     * Default-name for a step: page.name + {@value #STEPNAME_DELIMITTER} + {@value #STEPNAME_DEFAULT_STEPNUMBER}
     * @param page
     * @return
     *
     * @deprecated The default step-name is build by the agent as [Job-name]-[running number], f.e.: TestJob-1, TestJob-2. This method should work exactly like the agents default naming.
     */
    String getDefaultStepNameForPage(Page page) {
        return page.name + STEPNAME_DELIMITTER + STEPNAME_DEFAULT_STEPNUMBER
    }

}
