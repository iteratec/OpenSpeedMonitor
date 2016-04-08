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

package de.iteratec.osm.batch

/**
 * BatchActivity
 * Representation of a batch activity like job deletion
 */
class BatchActivity {

    String domain = ""
    long idWithinDomain
    String name
    Activity activity
    Date startDate = new Date()
    Date endDate
    Date lastUpdated
    Status status
    String stage
    String progressWithinStage
    String progress
    Integer failures = 0
    Integer successfulActions = 0
    String lastFailureMessage = ""

    /**There is a bug which casues Domains which extending from other Objects to fail
     * in tests, so that dynamic fileds will not be created. As a workaround we will delegate all Oberserver request to an observable
     **/
    Observable observable = new Observable()
    static transients = ['observable']


    static mapping = {
    }

    static constraints = {
        domain(nullable: false)
        idWithinDomain(nullable:true)
        name(nullable: false)
        activity(nullable: false)
        startDate(nullable: false)
        status(nullable: false)
        progress(nullable: false)
        successfulActions(nullable: false)
        stage(nullable: true)
        progressWithinStage(nullable: true)
        failures(nullable: false)
        lastFailureMessage(nullable: true)
        endDate(nullable: true)
    }

    public void setChanged(){
        observable.setChanged()
    }

    public void notifyObservers(){
        observable.notifyObservers(this)
    }

    public void addObserver(BatchActivityService o){
        observable.addObserver(o)
    }

    @Override
	public String toString() {
		return "Domain: $domain Progress: $progress";
	}

    /**
     *  Updates a BatchActivity with values from the given map and notify the Service to save it.
     *  Multiple calls within the save interval from BatchActivityService will only save the last call.
     *
     * @param BatchActivity BatchActivity to update
     * @param map with following possible entries:
     *      <li>"errors": Integer,</li>
     *      <li>"failures": Integer,</li>
     *      <li>"lastFailureMessage": String,</li>
     *      <li>"progress": String,</li>
     *      <li>"progressWithinStage": String,</li>
     *      <li>"stage": String,</li>
     *      <li>"status": Status,</li>
     *      <li>"successfulActions": Integer,</li>
     *      <li>"endDate": Date</li>
     */
    public void updateStatus(Map<String, Object> map) {
        List<String> allowed = ["errors", "failures", "lastFailureMessage", "progress", "progressWithinStage", "stage", "status", "successfulActions", "endDate"]
        map.each { key, value ->
            if (allowed.contains(key)) {
                this[key] = value
            } else {
                log.error("$key not allowed for ${this.class}")
            }
        }
        setChanged()
        notifyObservers()
    }
}
