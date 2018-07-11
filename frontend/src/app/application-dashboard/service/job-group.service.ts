import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ReplaySubject} from "rxjs/index";
import {JobGroupDTO} from "../model/job-group.model";

@Injectable({
  providedIn: 'root'
})
export class JobGroupService {
  activeOrRecentlyMeasured$ = new ReplaySubject<JobGroupDTO[]>(1);

  constructor(private  http: HttpClient) {
    this.updateActiveOrRecentlyMeasured();
  }

  updateActiveOrRecentlyMeasured() {
    this.http.get<JobGroupDTO[]>("/jobGroup/getAllActiveAndAllRecent")
      .subscribe(next => this.activeOrRecentlyMeasured$.next(next), error => this.handleError(error));
  }

  handleError(error: any) {
    console.log(error);
  }
}
