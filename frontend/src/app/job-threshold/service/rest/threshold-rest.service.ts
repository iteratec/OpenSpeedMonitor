import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import {Observable} from "rxjs/internal/Observable";


const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})

export class ThresholdRestService {

  private baseUrl = '/job';  // URL

  constructor(private http: HttpClient) {
    this.fetchData()
  }

  fetchData() {
  return this.getMeasurands();
  //this.getMeasuredEvents(this.scriptId);
  }


  /** GET heroes from the server */
  getMeasurands (): Observable<any> {
    const url = `${this.baseUrl}/getAllMeasurands`;
    return this.http.get<any>(url);

  }

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




}
