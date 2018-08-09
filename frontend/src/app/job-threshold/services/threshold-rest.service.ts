import {Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {ReplaySubject} from "rxjs/index";
import {Measurand} from "../models/measurand.model";
import {MeasuredEvent} from "../models/measured-event.model";
import {ThresholdForJob} from "../models/threshold-for-job.model";
import {Threshold} from "../models/threshold.model";
import {ActualMeasurandsService} from "./actual-measurands.service";
import {ActualThresholdsForJobService} from "./actual-thresholds-for-job.service";

@Injectable({
  providedIn: 'root'
})

export class ThresholdRestService {

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

    this.http.post(url,  params ).subscribe(response => {
      let newId : number=  Number(Object.values(response));
      this.actualThreshold.id= newId;
      self.actualThresholdsforJobList.addToActualThresholdsforJob(this.actualMeasuredEventId, this.actualThreshold);
    });
  }

  /** GET Script */
  getScritpt () {
    let self = this;
    let params = new HttpParams().set('jobId', this.actualJobId.toString());
    const url =`/job/getCiScript`;
    this.http.get(url, { params: params, responseType: 'text' })
      .subscribe(result =>  self.download(result), error => this.handleError(error)) ;
  }

  download(data) {
    let fileName = "CI_Script_" + this.actualJobId + ".groovy";
    let blob = new Blob([data], {type: 'text/plain;charset=utf-8'});
    this.saveData(blob, fileName);
  }

  saveData(blob, fileName) {
    let a = document.createElement("a");
    document.body.appendChild(a);
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
