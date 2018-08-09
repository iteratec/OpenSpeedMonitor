import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ReplaySubject} from "rxjs/index";
import {PageDto} from "../models/page.model";
import {MetricsDto} from "../models/metrics.model";
import {PageCsiDto} from "../models/page-csi.model";
import {ApplicationCsiListDTO} from "../models/csi-list.model";
import {ApplicationDTO} from "../models/application.model";
import {PageCsiResponse} from "../models/page-csi-response.model";

@Injectable()
export class ApplicationDashboardService {
  metrics$: ReplaySubject<MetricsDto[]> = new ReplaySubject<MetricsDto[]>(1);
  pages$: ReplaySubject<PageDto[]> = new ReplaySubject<PageDto[]>(1);
  csiValues$: ReplaySubject<ApplicationCsiListDTO> = new ReplaySubject<ApplicationCsiListDTO>(1);
  pageCsis$: ReplaySubject<PageCsiResponse> = new ReplaySubject<PageCsiResponse>(1);
  activeOrRecentlyMeasured$ = new ReplaySubject<ApplicationDTO[]>(1);

  constructor(private http: HttpClient) {
    this.updateActiveOrRecentlyMeasured()
  }

  updateActiveOrRecentlyMeasured() {
    this.http.get<ApplicationDTO[]>("/applicationDashboard/rest/getAllActiveAndAllRecent")
      .subscribe(next => this.activeOrRecentlyMeasured$.next(next), error => this.handleError(error));
  }

  updateApplicationData(application: ApplicationDTO) {
    const params = this.createParams(application.id);
    this.updateMetricsForApplication(params);
    this.updatePagesForApplication(params);
    this.updateCsiForApplication(params);
    this.updateCsiForPages(params);
  }

  private updateMetricsForApplication(params) {
    this.http.get<MetricsDto[]>('/applicationDashboard/rest/getMetricsForApplication', {params: params})
      .subscribe((response: MetricsDto[]) => this.metrics$.next(response), error => this.handleError(error));
  }

  private updatePagesForApplication(params) {
    this.http.get<PageDto[]>('/applicationDashboard/rest/getPagesForApplication', {params: params})
      .subscribe((response: PageDto[]) => this.pages$.next(response), error => this.handleError(error))
  }

  private updateCsiForApplication(params) {
    this.csiValues$.next({
      csiDtoList: [{csiDocComplete: 0, csiVisComplete: 0, date: null}],
      hasCsiConfiguration: false,
      isLoading: true
    });
    this.http.get<ApplicationCsiListDTO>('/applicationDashboard/rest/getCsiValuesForApplication', {params: params})
      .subscribe((response: ApplicationCsiListDTO) => this.csiValues$.next(response), error => this.handleError(error));
  }

  private updateCsiForPages(params) {
    this.pageCsis$.next({pageCsis: [], isLoading: true});
    this.http.get<PageCsiDto[]>('/applicationDashboard/rest/getCsiValuesForPages', {params: params})
      .subscribe((response: PageCsiDto[]) => {
        this.pageCsis$.next({pageCsis: response, isLoading: false});
      }, error => this.handleError(error));
  }


  private handleError(error: any) {
    console.log(error);
  }

  private createParams(applicationId: number) {
    return {
      applicationId: applicationId ? applicationId.toString() : ""
    }
  }

}
