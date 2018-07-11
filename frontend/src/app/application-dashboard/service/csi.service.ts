import {Injectable} from '@angular/core';
import {ReplaySubject} from "rxjs/internal/ReplaySubject";
import {HttpClient, HttpParams} from "@angular/common/http";
import {ApplicationCsiListDTO} from "../model/csi-list.model";
import {ApplicationDTO} from "../model/application.model";

@Injectable()
export class CsiService {

  public csiValues$ = new ReplaySubject<ApplicationCsiListDTO>(1);

  constructor(private http: HttpClient) {
  }

  getCsiForApplication(application: ApplicationDTO) {
    const params = new HttpParams().set('applicationId', application.id.toString());
    this.http.get<ApplicationCsiListDTO>('/applicationDashboard/getCsiValuesForApplication', {params: params})
      .subscribe(response => this.csiValues$.next(response), error => this.handleError(error));
  }

  handleError(error: any) {
    console.log(error);
  }
}
