import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, ReplaySubject} from "rxjs/index";
import {PageDto} from "../models/page.model";
import {MetricsDto} from "../models/metrics.model";
import {PageCsiDto} from "../models/page-csi.model";
import {ApplicationDTO} from "../models/application.model";

@Injectable()
export class ApplicationDashboardService {
  metrics$: ReplaySubject<MetricsDto[]> = new ReplaySubject<MetricsDto[]>(1);
  pageCsis$: ReplaySubject<PageCsiDto[]> = new ReplaySubject<PageCsiDto[]>(1);

  constructor(private http: HttpClient) {
  }

  getPagesForJobGroup(applicationId: number): Observable<PageDto[]> {
    return this.http.get<PageDto[]>('/applicationDashboard/rest/getPagesForApplication', {
      params: {
        applicationId: applicationId ? applicationId.toString() : ""
      }
    });
  }

  updateApplicationPages(application: ApplicationDTO) {
    this.updateMetricsForApplication(application.id);
    this.updateCsiForPages(application.id);
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

  private updateCsiForPages(applicationId: number) {
    this.http.get<PageCsiDto[]>('/applicationDashboard/rest/getCsiValuesForPages', {
      params: {
        applicationId: applicationId ? applicationId.toString() : ""
      }
    }).subscribe((response: PageCsiDto[]) => {
      this.pageCsis$.next(response)
    });
  }

}
