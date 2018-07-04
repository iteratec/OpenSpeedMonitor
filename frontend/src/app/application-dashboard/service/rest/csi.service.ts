import {Injectable} from '@angular/core';
import {ReplaySubject} from "rxjs/internal/ReplaySubject";
import {HttpClient, HttpParams} from "@angular/common/http";
import {JobGroupDTO} from "../../../shared/model/job-group.model";
import {CsiListDTO} from "../../model/csi-list.model";

@Injectable()
export class CsiService {

  public csiValues$ = new ReplaySubject<CsiListDTO>(1);

  constructor(private http: HttpClient) {
  }

  getCsiForJobGroup(jobGroup: JobGroupDTO) {
    let params = new HttpParams().set('jobGroupId', jobGroup.id.toString());
    this.http.get<CsiListDTO>('/csiDashboard/getCSIForActiveOrRecentlyMeasuredJobGroups', {params: params})
      .subscribe(response => this.csiValues$.next(response), error => this.handleError(error));
  }

  handleError(error: any) {
    console.log(error);
  }
}
