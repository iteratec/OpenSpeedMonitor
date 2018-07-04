import {Injectable} from '@angular/core';
import {ReplaySubject} from "rxjs/internal/ReplaySubject";
import {HttpClient, HttpParams} from "@angular/common/http";
import {JobGroupDTO} from "../../../shared/model/job-group.model";
import {ApplicationCsiListDTO} from "../../model/csi-list.model";

@Injectable()
export class CsiService {

  public csiValues$ = new ReplaySubject<ApplicationCsiListDTO>(1);

  constructor(private http: HttpClient) {
  }

  getCsiForJobGroup(application: JobGroupDTO) {
    let params = new HttpParams().set('jobGroupId', application.id.toString());
    this.http.get<ApplicationCsiListDTO>('/csiDashboard/getCSIForActiveOrRecentlyMeasuredJobGroups', {params: params})
      .subscribe(response => this.csiValues$.next(response), error => this.handleError(error));
  }

  handleError(error: any) {
    console.log(error);
  }
}
