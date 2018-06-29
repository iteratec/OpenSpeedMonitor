import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/index";
import {IPage} from "../model/page.model";

@Injectable({
  providedIn: 'root'
})
export class ApplicationDashboardService {

  constructor(private http: HttpClient) {
  }

  getPagesForJobGroup(jobGroupId: number): Observable<IPage[]> {
    return this.http.get<IPage[]>("/applicationDashboard/getPagesForJobGroup", {
      params: {
        jobGroupId: jobGroupId.toString()
      }
    });
  }

}
