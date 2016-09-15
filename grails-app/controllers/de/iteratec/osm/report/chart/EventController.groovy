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

import org.joda.time.DateTime
import org.springframework.web.servlet.support.RequestContextUtils

/**
 * EventController
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
class EventController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    EventService eventService

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max, String filterDate, String filterTime, String filterName, String filterDescription) {
        def maxDefault = 100
        if (max) maxDefault = max
        params.max = maxDefault
        DateTime timeToFilterStart
        DateTime timeToFilterEnd
        def errorList = []
        List<Event> result
        int count
        try{
            if(filterTime) {
                timeToFilterStart=new DateTime(filterDate+"T"+filterTime).minusMinutes(1)
                timeToFilterEnd = timeToFilterStart.plusMinutes(2)
            } else{
                timeToFilterStart=new DateTime(filterDate)
                timeToFilterEnd = timeToFilterStart.plusDays(1)
            }
        }catch(IllegalArgumentException ex){
            errorList << message(code: 'event.dateTime.notValid', default: 'Unable to parse date input.\n')
            return [eventInstanceList: result, eventInstanceTotal: count, filterDate:filterDate?filterDate:"", filterTime:filterTime?filterTime:"",
                    filterName:filterName?filterName:"", filterDescription:filterDescription?filterDescription:"", errorList:errorList]
        }
        if(!filterDate && !filterTime && !filterName && !filterDescription) {
            result = Event.list(params)
            count = Event.list().size()
        }
        else {
            result = Event.createCriteria().list(params) {
                if(filterName) ilike("shortName", "%"+filterName+"%")
                if(filterDescription) ilike("description", "%"+filterDescription+"%")
                if(timeToFilterStart && timeToFilterEnd) between("eventDate",timeToFilterStart.toDate(),timeToFilterEnd.toDate())
            }
            count = Event.createCriteria().list {
                if(filterName) ilike("shortName", "%"+filterName+"%")
                if(filterDescription) ilike("description", "%"+filterDescription+"%")
                if(timeToFilterStart && timeToFilterEnd) between("eventDate",timeToFilterStart.toDate(),timeToFilterEnd.toDate())
            }.size()
        }


        [eventInstanceList: result, eventInstanceTotal: count, filterDate:filterDate?filterDate:"", filterTime:filterTime?filterTime:"",
                               filterName:filterName?filterName:"", filterDescription:filterDescription?filterDescription:"", errorList:errorList]

    }

    def create() {
        [eventInstance: new Event(params)]
    }

    def save() {
        combineDateAndTime(params)
        eventService.saveEvent(params) {
            action.success = { Event eventInstance ->
                flash.message = message(code: 'default.created.message', args: [message(code: 'event.label', default: 'Event'), eventInstance.id])
                redirect(action: "show", id: eventInstance.id)
            }
            action.failure = { Event eventInstance ->
                render(view: "create", model: [eventInstance: eventInstance])
            }
        }

    }

    def show() {
        def eventInstance = Event.get(params.id)
        if (!eventInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), params.id])
            redirect(action: "list")
            return
        }
        [eventInstance: eventInstance]
    }

    def edit() {
        def eventInstance = Event.get(params.id)
        if (!eventInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), params.id])
            redirect(action: "list")
            return
        }

        [eventInstance: eventInstance]
    }

    def update() {
        def eventInstance = Event.get(params.id)
        if (!eventInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), params.id])
            redirect(action: "list")
            return
        }

        eventInstance.jobGroups.clear()
        combineDateAndTime(params)

        eventService.updateEvent(eventInstance, params.clone()) {
            action.success = {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'event.label', default: 'Event'), eventInstance.id])
                redirect(action: "show", id: eventInstance.id)
            }
            action.failure = {
                render(view: "edit", model: [eventInstance: eventInstance])
            }
        }
    }

    def delete() {
        def eventInstance = Event.get(params.id)
        if (!eventInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'event.label', default: 'Event'), params.id])
            redirect(action: "list")
            return
        }

        eventService.deleteEvent(eventInstance) {
            action.success = {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'event.label', default: 'Event'), params.id])
                redirect(action: "list")
            }
            action.failure = { exception ->
                flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'event.label', default: 'Event'), params.id])
                redirect(action: "show", id: params.id)
            }
        }
    }

    /**
     * Combines time and date within the param list, where time ist 'time' and date is 'eventDate'
     * @param params
     */
    private def combineDateAndTime(def params) {
        //Convert english date format to german, for passing validation
        //The gsp passes the time and the date separately so we need to combine these two
        if(params['eventDate'] && params['time']) {
            params['eventDate'] = params['eventDate'] + " " + params['time']
            def formatter
            params.remove('time')
            def locale = RequestContextUtils.getLocale(request)
            switch (locale) {
                case Locale.GERMANY:
                case Locale.GERMAN:
                    formatter = "dd.MM.yyyy HH:mm"
                    break;
                default:
                    formatter = "yyyy-MM-dd HH:mm"
            }
            return params['eventDate'] = Date.parse(formatter, params['eventDate'] as String)
        } else {
            return false
        }
    }
}
