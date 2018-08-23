import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {EMPTY, Observable} from 'rxjs';
import {Measurand} from '../models/measurand.model';
import {MeasuredEvent} from '../models/measured-event.model';
import {ThresholdGroup} from '../models/threshold-for-job.model';
import {Threshold} from '../models/threshold.model';
import {catchError, map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})

export class ThresholdRestService {

  private jobId: number;

  constructor(private http: HttpClient) {
  }

  getMeasurands(): Observable<Measurand[]> {
    const url = `/job/getAllMeasurands`;
    return this.http.get<Measurand[]>(url).pipe(catchError(error => this.handleError(error)));
  }

  getMeasuredEvents(scriptId: number, jobId: number): Observable<MeasuredEvent[]> {
    this.jobId = jobId;
    const url = `/script/getMeasuredEventsForScript?scriptId=${scriptId}`;
    return this.http.get<MeasuredEvent[]>(url).pipe(
      catchError(error => this.handleError(error))
    )
  }

  getThresholdGroups(jobId: number): Observable<ThresholdGroup[]> {
    const url = `/job/getThresholdsForJob?jobId=${jobId}` ;
    return this.http.get<ThresholdGroup[]>(url).pipe(catchError(error => this.handleError(error)));
  }

  deleteThreshold(threshold: Threshold): Observable<void> {
    const url = "/threshold/deleteThreshold" ;
    let formData = new HttpParams();
    formData.append('thresholdId', threshold.id.toString());
    return this.http.post(url, formData).pipe(catchError(error => this.handleError(error)));
  }

  updateThreshold(threshold: Threshold): Observable<void> {
    const url = "/threshold/updateThreshold" ;
    let params = new HttpParams().set('thresholdId', threshold.id.toString());
    params = params.set('measurand', threshold.measurand.name.toString());
    params = params.set('measuredEvent', threshold.measuredEvent.id.toString());
    params = params.set('lowerBoundary', threshold.lowerBoundary.toString());
    params = params.set('upperBoundary', threshold.upperBoundary.toString());

    return this.http.post(url, params).pipe(catchError(error => this.handleError(error)));
  }

  addThreshold(threshold: Threshold): Observable<number> {
    const url = "/threshold/createThreshold" ;
    let params = new HttpParams().set('job', this.jobId.toString());
    params = params.set('measurand', threshold.measurand.name.toString());
    params = params.set('measuredEvent', threshold.measuredEvent.id.toString());
    params = params.set('lowerBoundary', threshold.lowerBoundary.toString());
    params = params.set('upperBoundary', threshold.upperBoundary.toString());
    return this.http.post(url, params).pipe(
      catchError(error => this.handleError(error)),
      map(response => Number(Object.values(response)))
    );
  }

  getScript(): Observable<string> {
    let params = new HttpParams().set('jobId', this.jobId.toString());
    const url =`/job/getCiScript`;
    return this.http.get(url, {params: params, responseType: 'text'}).pipe(
      catchError(error => this.handleError(error))
    );
  }

  handleError(error: any): Observable<any> {
    console.log(error);
    return EMPTY;
  }
}
