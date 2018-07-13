import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ReplaySubject} from "rxjs/index";
import {PageDto} from "../models/page.model";
import {MetricsDto} from "../models/metrics.model";
import {ApplicationCsiListDTO} from "../models/csi-list.model";

@Injectable()
export class ApplicationDashboardService {
  metrics$: ReplaySubject<MetricsDto[]> = new ReplaySubject<MetricsDto[]>(1);
  pages$: ReplaySubject<PageDto[]> = new ReplaySubject<PageDto[]>(1);
  csiValues$ = new ReplaySubject<ApplicationCsiListDTO>(1);

  constructor(private http: HttpClient) {
  }

  updateMetricsForApplication(applicationId: number) {
    this.http.get<MetricsDto[]>('/applicationDashboard/rest/getMetricsForApplication', this.createParams(applicationId))
      .subscribe((response: MetricsDto[]) => this.metrics$.next(response), error => this.handleError(error));
  }

  updatePagesForApplication(applicationId: number) {
    this.http.get<PageDto[]>('/applicationDashboard/rest/getPagesForApplication', this.createParams(applicationId))
      .subscribe((response: PageDto[]) => this.pages$.next(response), error => this.handleError(error))
  }

  updateCsiForApplication(applicationId: number) {
    this.http.get<ApplicationCsiListDTO>('/applicationDashboard/rest/getCsiValuesForApplication', this.createParams(applicationId))
      .subscribe((response: ApplicationCsiListDTO) => this.csiValues$.next(response), error => this.handleError(error));
  }

  private handleError(error: any) {
    console.log(error);
  }

  private createParams(applicationId: number) {
    return {
      params: {
        applicationId: applicationId ? applicationId.toString() : ""
      }
    }
  }

}

