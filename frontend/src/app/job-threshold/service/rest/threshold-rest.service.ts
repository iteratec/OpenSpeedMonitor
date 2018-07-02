import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import {Observable} from "rxjs/internal/Observable";
import { mergeMap } from 'rxjs/operators';

import {Measurand} from '../model/measurand.model'
import {MeasuredEvent} from '../model/measured-event.model'
import {ThresholdForJob} from '../model/threshold-for-job.model'
import {Threshold} from "../model/threshold.model";
import {Subject} from "rxjs/internal/Subject";




const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})

export class ThresholdRestService {

  private baseUrl = '/job';  // URL

  constructor(private http: HttpClient) {}

  /** GET Measurands */
  getMeasurands ()/*: Observable< Measurand[] >*/ {
    const url = `${this.baseUrl}/getAllMeasurands`;
    return this.http.get< Measurand[]>(url);
  }

  /** GET MeasuredEvents */
  getMeasuredEvents (scriptId: number)/*: Observable<MeasuredEvent[]> */{
    /*console.log("getMeasuredEvents scriptId: " + scriptId);*/
    const url = `/script/getMeasuredEventsForScript?scriptId=${scriptId}`;
    return this.http.get<MeasuredEvent[]>(url) ;
  }

  /** GET Thresholds For a Job */
  getThresholdsForJob (jobId: number)/*: Observable<ThresholdForJob[]> */{
    /*console.log("getThresholds jobId: " + jobId);*/
    const url = `/job/getThresholdsForJob?jobId=${jobId}` ;
    return this.http.get<ThresholdForJob[]>(url) ;
  }

    /*getThresholds: function () {
  this.activeMeasuredEvents = [];
  var self = this;
  this.getThresholdsForJob(this.jobId).success(function (result) {
    result.forEach(function (resultEvent) {
      var thresholdsForEvent = [];
      resultEvent.thresholds.forEach(function (threshold) {
        thresholdsForEvent.push({
          threshold: threshold,
          edit: false,
          saved: true
        })
      });
      self.activeMeasuredEvents.push({
        measuredEvent: self.measuredEvents.find(function (element) {
          return element.id === resultEvent.measuredEvent.id;
        }),
        thresholdList: thresholdsForEvent
      })
    })
  }).error(function (e) {
    console.log(e);
  });
}*/

  /*getHero(id: number): Observable<Hero> {
    const url = `${this.heroesUrl}/${id}`;
    return this.http.get<Hero>(url).pipe(
      tap(_ => this.log(`fetched hero id=${id}`)),
      catchError(this.handleError<Hero>(`getHero id=${id}`))
    );
  }*/

 /* getMeasurands() {
  var self = this;
  $.ajax({
    type: 'GET',
    url: "/job/getAllMeasurands",
    data: {},
    success: function (result) {
      result.forEach(function (measurand) {
        measurand.translatedName = OpenSpeedMonitor.i18n.measurands[measurand.name];
      });

      self.measurands = result;
    },
    error: function () {
      return ""
    }
  });


}*/
  /*getMeasuredEvents: function (scriptId) {
  var self = this;
  if (scriptId) {
    $.ajax({
      type: 'GET',
      url: "/script/getMeasuredEventsForScript",
      data: {scriptId: scriptId},
      success: function (result) {
        self.measuredEvents = result;
        self.copiedMeasuredEvents = self.measuredEvents.slice();
        self.measuredEventCount = self.measuredEvents.length;
        self.getThresholds();
      },
      error: function () {
        return ""
      }
    });
  }
},*/




}
