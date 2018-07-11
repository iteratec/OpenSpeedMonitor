import {Injectable} from '@angular/core';
import {ReplaySubject} from "rxjs/internal/ReplaySubject";
import {HttpClient, HttpParams} from "@angular/common/http";
import {JobGroupDTO} from "../../shared/models/job-group.model";
import {ApplicationCsiListDTO} from "../models/csi-list.model";

@Injectable()
export class CsiService {

  public csiValues$ = new ReplaySubject<ApplicationCsiListDTO>(1);

  constructor(private http: HttpClient) {
  }

  getCsiForApplication(application: JobGroupDTO) {
    const params = new HttpParams().set('applicationId', application.id.toString());
    this.http.get<ApplicationCsiListDTO>('/applicationDashboard/getCsiValuesForApplication', {params: params})
      .subscribe(response => this.csiValues$.next(response), error => this.handleError(error));
  }

  handleError(error: any) {
    console.log(error);
  }
}
