import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/index";

@Injectable({
  providedIn: 'root'
})
export class ApplicationDashboardService {

  constructor(private http: HttpClient) {
  }

  getPagesForJobGroup(jobGroupId: number): Observable<any> {
    return this.http.get<any>("/applicationDashboard/getPagesForJobGroup", {
      params: {
        jobGroupId: jobGroupId.toString()
      }
    });
  }

}
