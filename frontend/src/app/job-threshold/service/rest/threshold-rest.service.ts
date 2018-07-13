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
import {ActualMeasurandsService} from  "../actual-measurands.service"
import {Subject} from "rxjs/internal/Subject";
import {log} from "util";

@Injectable({
  providedIn: 'root'
})

export class ThresholdRestService {

  public thresholdsForJob$ = new ReplaySubject<ThresholdForJob[]>(1);
  //public measurands$ = new ReplaySubject<Measurand[]>(1);
  public measuredEvents$ = new ReplaySubject<MeasuredEvent[]>(1);

  public actualJobId : number;
  private baseUrl = '/job';  // URL

  constructor(private http: HttpClient, private actualMeasurandService: ActualMeasurandsService) {
    this.getMeasurands();
  }

  /** GET Measurands */
  getMeasurands () {
    const url = `/job/getAllMeasurands`;
    this.http.get< Measurand[]>(url).subscribe((measurands:Measurand[]) => this.actualMeasurandService.setActualMeasurands(measurands));
  }

  /** GET MeasuredEvents */
  getMeasuredEvents (scriptId: number){
    /*console.log("getMeasuredEvents scriptId: " + scriptId);*/
    const url = `/script/getMeasuredEventsForScript?scriptId=${scriptId}`;
    this.http.get<MeasuredEvent[]>(url)
      .subscribe(next => this.measuredEvents$.next(next), error => this.handleError(error)) ;
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

  /** GET Script */
  getScritpt () {
    var self = this;
    let params = new HttpParams().set('jobId', this.actualJobId.toString());
    const url =`/job/getCiScript`;
    this.http.get(url, { params: params, responseType: 'text' })
      .subscribe(result =>  self.download(result), error => this.handleError(error)) ;
  }

  download(data) {
    var fileName = "CI_Script_" + this.actualJobId + ".groovy";
    var blob = new Blob([data], { type: 'text/plain;charset=utf-8' });
    this.saveData(blob, fileName);
  }

  saveData(blob, fileName) {
    var a = document.createElement("a");
    document.body.appendChild(a);
    //a.style = "display: none";
    let url = window.URL.createObjectURL(blob);
    a.href = url;
    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  handleError(error: any){
    console.log(error);
  }
}
