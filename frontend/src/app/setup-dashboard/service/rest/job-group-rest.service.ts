import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UrlStore} from '../../../common/app.url-store'
import {Observable} from "rxjs/internal/Observable";
import {JobGroupToPagesMappingDto} from "../../../common/model/job-group-to-page-mapping.model";

@Injectable({
  providedIn: 'root'
})
export class JobGroupRestService {

  constructor(private http: HttpClient) {
  }

  getActiveJobGroups() {
    return this.http.get(UrlStore.GET_ACTIVE_JOB_URL)
  }

  getJobGroupToPagesMapDto(from: string, to: string): Observable<JobGroupToPagesMappingDto[]> {
    return this.http.get<JobGroupToPagesMappingDto[]>('/jobGroup/getJobGroupsWithPages', {
      params: {
        from: from,
        to: to
      }
    });
  }
}
