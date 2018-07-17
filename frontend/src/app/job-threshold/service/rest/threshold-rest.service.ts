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
import {ActualMeasurandsService} from  "../actual-measurands.service";
import {ActualThresholdsForJobService} from  "../actual-thresholds-for-job.service"
import {Subject} from "rxjs/internal/Subject";
import {log} from "util";

@Injectable({
  providedIn: 'root'
})

export class ThresholdRestService {

  //public thresholdsForJob$ = new ReplaySubject<ThresholdForJob[]>(1);
  public measuredEvents$ = new ReplaySubject<MeasuredEvent[]>(1);
  public actualJobId : number;
  public actualThresholdId: number;
  public actualMeasuredEventId: number;
  public actualThreshold: Threshold;


  constructor(private http: HttpClient,
              private actualMeasurandService: ActualMeasurandsService,
              private actualThresholdsforJobList: ActualThresholdsForJobService) {
    this.getMeasurands();
  }

  /** GET Measurands */
  getMeasurands () {
    const url = `/job/getAllMeasurands`;
    this.http.get< Measurand[]>(url).subscribe((measurands:Measurand[]) => this.actualMeasurandService.setActualMeasurands(measurands));
  }

  /** GET MeasuredEvents */
  getMeasuredEvents (scriptId: number, jobId: number){
    this.actualJobId = jobId;
    const url = `/script/getMeasuredEventsForScript?scriptId=${scriptId}`;
    this.http.get<MeasuredEvent[]>(url)
      .subscribe(next => {
        this.measuredEvents$.next(next);
        this.getThresholdsForJob(this.actualJobId)
      }, error => this.handleError(error)) ;
  }

  /** GET Thresholds For a Job */
  getThresholdsForJob (jobId: number){
    const url = `/job/getThresholdsForJob?jobId=${jobId}` ;
    this.http.get<ThresholdForJob[]>(url)
      //.subscribe(next => this.thresholdsForJob$.next(next), error => this.handleError(error)) ;
      .subscribe(next => this.actualThresholdsforJobList.setActualThresholdsforJobList(next), error => this.handleError(error)) ;
  }

  /** DELETE Threshold */
  deleteThreshold (threshold: Threshold){
    this.actualThreshold = threshold;
    const url = "/threshold/deleteThreshold" ;
    let self= this;
    let formData = new FormData()
    formData.append("thresholdId", threshold.id.toString())
    this.http.post(url, formData).subscribe(() => {
      console.log("state: " + threshold.measuredEvent.state);
      self.actualThresholdsforJobList.deleteFromActualThresholdsforJob(this.actualThreshold);
    });
  }

  /** Edit Threshold */
  editThreshold (threshold: Threshold){
    this.actualThresholdId = threshold.id;
    this.actualThreshold = threshold;
    const url = "/threshold/updateThreshold" ;
    let self= this;
    let params = new HttpParams().set('thresholdId', threshold.id.toString());
    params = params.set('measurand', threshold.measurand.name.toString());
    params = params.set('measuredEvent', threshold.measuredEvent.id.toString());
    params = params.set('lowerBoundary', threshold.lowerBoundary.toString());
    params = params.set('upperBoundary', threshold.upperBoundary.toString());

    this.http.post(url, params).subscribe(() => {
        //self.getThresholdsForJob(self.actualJobId);
      self.actualThresholdsforJobList.editThresholdOfActualThresholdsforJob(this.actualThresholdId, this.actualThreshold );
      }
    );
  }

  /** Add Threshold */
  addThreshold (threshold: Threshold){
    this.actualThreshold = threshold;
    this.actualMeasuredEventId = threshold.measuredEvent.id;
    let self= this;
    const url = "/threshold/createThreshold" ;
    let params = new HttpParams().set('job', this.actualJobId.toString());
    params = params.set('measurand', threshold.measurand.name.toString());
    params = params.set('measuredEvent', threshold.measuredEvent.id.toString());
    params = params.set('lowerBoundary', threshold.lowerBoundary.toString());
    params = params.set('upperBoundary', threshold.upperBoundary.toString());

    this.http.post(url, params).subscribe(() => {
        //self.getThresholdsForJob(self.actualJobId);
        if (threshold.measuredEvent.state=="new") {
          self.actualThresholdsforJobList.addThresholdForJobToActualThresholdsforJob(this.actualMeasuredEventId, this.actualThreshold);
        } else {
          self.actualThresholdsforJobList.addThresholdToActualThresholdsforJob(this.actualMeasuredEventId, this.actualThreshold);
        }
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
