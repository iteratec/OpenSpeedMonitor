import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import {ReplaySubject} from "rxjs/index";
import { catchError, map, tap } from 'rxjs/operators';
import {Observable} from "rxjs/internal/Observable";
import { mergeMap } from 'rxjs/operators';

import {Measurand} from '../model/measurand.model'
import {MeasuredEvent} from '../model/measured-event.model'
import {ThresholdForJob} from '../model/threshold-for-job.model'
import {Threshold} from "../model/threshold.model";
import {Subject} from "rxjs/internal/Subject";
import {log} from "util";



@Injectable({
  providedIn: 'root'
})

export class ThresholdRestService {

  public thresholdsForJob$ = new ReplaySubject<ThresholdForJob[]>(1);
  public measurands$ = new ReplaySubject<Measurand[]>(1);

  public actualJobId : number;
  private baseUrl = '/job';  // URL

  constructor(private http: HttpClient) {

  }

  /** GET Measurands */
  getMeasurands ()/*: Observable< Measurand[] >*/ {
    const url = `/job/getAllMeasurands`;
    this.http.get< Measurand[]>(url).subscribe( next => this.measurands$.next(next), error => this.handleError(error));
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
    this.http.get<ThresholdForJob[]>(url)
      .subscribe(next => this.thresholdsForJob$.next(next), error => this.handleError(error)) ;
  }

  /** DELETE Threshold */
  deleteThreshold (thresholdId: string){
    console.log("deleteThreshold thresholdId " + thresholdId);
    const url = "/threshold/deleteThreshold" ;
    let self= this;
    let formData = new FormData()
    formData.append("thresholdId", thresholdId)
    this.http.post(url, formData).subscribe(() => {
      console.log("delete server Response: " + self.actualJobId);
      self.getThresholdsForJob(self.actualJobId);
      }
    );
  }

  /** Edit Threshold */
  editThreshold (threshold: Threshold){
    const url = "/threshold/updateThreshold" ;
    let self= this;
    let params = new HttpParams().set('thresholdId', threshold.id.toString());
    params = params.set('measurand', threshold.measurand.name.toString());
    params = params.set('measuredEvent', threshold.measuredEvent.id.toString());
    params = params.set('lowerBoundary', threshold.lowerBoundary.toString());
    params = params.set('upperBoundary', threshold.upperBoundary.toString());

    this.http.post(url, params).subscribe(() => {
        console.log("self.actualJobId : " + self.actualJobId);
        self.getThresholdsForJob(self.actualJobId);
      }
    );
  }

  /** Add Threshold */
  addThreshold (threshold: Threshold){
    const url = "/threshold/createThreshold" ;
    let self= this;
    let params = new HttpParams().set('job', self.actualJobId.toString());
    params = params.set('measurand', threshold.measurand.name.toString());
    params = params.set('measuredEvent', threshold.measuredEvent.id.toString());
    params = params.set('lowerBoundary', threshold.lowerBoundary.toString());
    params = params.set('upperBoundary', threshold.upperBoundary.toString());

    this.http.post(url, params).subscribe(() => {
        console.log("self.actualJobId : " + self.actualJobId);
        self.getThresholdsForJob(self.actualJobId);
      }
    );
  }

  handleError(error: any){
    console.log(error);
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
