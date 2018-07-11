import {Injectable} from '@angular/core';
import {Observable} from "rxjs/index";
import {HttpClient} from "@angular/common/http";
import {JobGroupToPagesMappingDto} from "../model/job-group-to-page-mapping.model";

@Injectable({
  providedIn: 'root'
})
export class JobGroupService {

  constructor(private http: HttpClient) {
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
