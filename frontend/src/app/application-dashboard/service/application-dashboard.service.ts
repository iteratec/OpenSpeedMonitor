import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, ReplaySubject} from "rxjs/index";
import {PageDto} from "../model/page.model";
import {MetricsDto} from "../model/metrics.model";

@Injectable()
export class ApplicationDashboardService {
  metrics$: ReplaySubject<MetricsDto[]> = new ReplaySubject<MetricsDto[]>(1);

  constructor(private http: HttpClient) {
  }

  getPagesForJobGroup(applicationId: number): Observable<PageDto[]> {
    return this.http.get<PageDto[]>('/applicationDashboard/rest/getPagesForApplication', {
      params: {
        applicationId: applicationId ? applicationId.toString() : ""
      }
    });
  }

  updateMetricsForApplication(applicationId: number) {
    this.http.get<MetricsDto[]>('/applicationDashboard/rest/getMetricsForApplication', {
      params: {
        applicationId: applicationId ? applicationId.toString() : ""
      }
    }).subscribe((response: MetricsDto[]) => {
      this.metrics$.next(response)
    });
  }

}
