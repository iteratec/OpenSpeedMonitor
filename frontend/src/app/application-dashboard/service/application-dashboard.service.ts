import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/index";
import {IPage} from "../model/page.model";

@Injectable()
export class ApplicationDashboardService {

  constructor(private http: HttpClient) {
  }

  getPagesForJobGroup(applicationId: number): Observable<IPage[]> {
    return this.http.get<IPage[]>("/applicationDashboard/getPagesForApplication", {
      params: {
        applicationId: applicationId.toString()
      }
    });
  }

}
