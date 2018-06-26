import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, ReplaySubject} from 'rxjs';
import {JobGroupDTO} from "../../../common/model/job-group.model";
import {JobGroupToPagesMappingDto} from '../../../common/model/job-group-to-page-mapping.model';

@Injectable({
  providedIn: 'root'
})
export class JobGroupService {

  public jobGroups$ = new ReplaySubject<JobGroupDTO[]>(1);

  constructor(private http: HttpClient) {
    this.updateActiveJobGroups()
  }

  updateActiveJobGroups() {
    this.http.get<JobGroupDTO[]>("jobGroup/getAllActive")
      .subscribe(next => this.jobGroups$.next(next), error => this.handleError(error));
  }

  handleError(error: any) {
    console.log(error);
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
