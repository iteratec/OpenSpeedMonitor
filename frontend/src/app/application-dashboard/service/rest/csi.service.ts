import {Injectable} from '@angular/core';
import {ReplaySubject} from "rxjs/internal/ReplaySubject";
import {CsiDTO} from "../../model/csi.model";
import {HttpClient, HttpParams} from "@angular/common/http";
import {JobGroupDTO} from "../../../shared/model/job-group.model";

@Injectable({
  providedIn: 'root'
})
export class CsiService {

  public csiValues$ = new ReplaySubject<CsiDTO[]>(1);

  constructor(private http: HttpClient) {
  }

  getCsiForJobGroup(jobGroup: JobGroupDTO) {
    console.log(job)
    let params = new HttpParams().set('jobGroupId', jobGroup.id.toString());
    this.http.get<CsiDTO[]>('csiDashboard/getCSIForActiveOrRecentlyMeasuredJobGroups', {params: params})
      .subscribe(next => this.csiValues$.next(next), error => this.handleError(error));
  }

  handleError(error: any) {
    console.log(error);
  }
}
