import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UrlStore } from '../../../common/app.url-store'
import {Observable} from "rxjs/internal/Observable";
import {IJobGroupToPagesMapping} from "../../../common/model/job-group-to-page-mapping.model";

@Injectable({
  providedIn: 'root'
})
export class JobGroupRestService {

  constructor(private http: HttpClient) {
  }

  getActiveJobGroups() {
    return this.http.get(UrlStore.GET_ACTIVE_JOB_URL)
  }
  getJobGroupToPagesMap(from: string, to: string):Observable<IJobGroupToPagesMapping[]> {
    return this.http.get<IJobGroupToPagesMapping[]>('/jobGroup/getJobGroupsWithPages', {params: {from: from, to: to}});
  }
}
