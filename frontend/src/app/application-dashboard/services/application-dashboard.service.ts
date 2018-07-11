import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, ReplaySubject} from "rxjs/index";
import {PageDto} from "../models/page.model";
import {MetricsDto} from "../models/metrics.model";

@Injectable()
export class ApplicationDashboardService {
  metrics$: ReplaySubject<MetricsDto[]> = new ReplaySubject<MetricsDto[]>(1);

  constructor(private http: HttpClient) {
  }

  getPagesForJobGroup(applicationId: number): Observable<PageDto[]> {
    return this.http.get<PageDto[]>("/applicationDashboard/getPagesForApplication", {
      params: {
        applicationId: applicationId ? applicationId.toString() : ""
      }
    });
  }

  updateMetricsForApplication(applicationId: number) {
    this.http.get<MetricsDto[]>('/applicationDashboard/getMetricsForApplication', {
      params: {
        applicationId: applicationId ? applicationId.toString() : ""
      }
    }).subscribe((response: MetricsDto[]) => {
      this.metrics$.next(response)
    });
  }

}
