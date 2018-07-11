import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ReplaySubject} from "rxjs/index";
import {JobGroupDTO} from "../../application-dashboard/model/job-group.model";

@Injectable({
  providedIn: 'root'
})
export class JobGroupService {
  public jobGroups$ = new ReplaySubject<JobGroupDTO[]>(1);

  constructor(private http: HttpClient) {
    this.updateActiveJobGroups()
  }

  updateActiveJobGroups() {
    this.http.get<JobGroupDTO[]>("/jobGroup/getAllActive")
      .subscribe(next => this.jobGroups$.next(next), error => this.handleError(error));
  }

  handleError(error: any) {
    console.log(error);
  }
}
