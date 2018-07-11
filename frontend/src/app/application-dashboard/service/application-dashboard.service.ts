import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, ReplaySubject} from "rxjs/index";
import {PageDto} from "../model/page.model";
import {MetricsDto} from "../model/metrics.model";
import {ApplicationDTO} from "../model/application.model";

@Injectable()
export class ApplicationDashboardService {
  metrics$: ReplaySubject<MetricsDto[]> = new ReplaySubject<MetricsDto[]>(1);
  activeOrRecentlyMeasured$ = new ReplaySubject<ApplicationDTO[]>(1);

  constructor(private http: HttpClient) {
    this.updateActiveOrRecentlyMeasured()
  }

  updateActiveOrRecentlyMeasured() {
    this.http.get<ApplicationDTO[]>("/applicationDashboard/getAllActiveAndAllRecent")
      .subscribe(next => this.activeOrRecentlyMeasured$.next(next), error => this.handleError(error));
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
    }).subscribe(
      (response: MetricsDto[]) =>
        this.metrics$.next(response),
      error =>
        this.handleError(error)
    );
  }

  handleError(error: any) {
    console.log(error);
  }
}
