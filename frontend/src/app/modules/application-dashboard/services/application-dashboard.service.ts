import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EMPTY, Observable, ReplaySubject, Subject} from "rxjs/index";
import {PageMetricsDto} from "../models/page-metrics.model";
import {PageCsiDto} from "../models/page-csi.model";
import {ApplicationCsiListDTO} from "../models/csi-list.model";
import {ApplicationDTO} from "../models/application.model";
import {catchError, map, switchMap} from "rxjs/internal/operators";
import {ResponseWithLoadingState} from "../models/response-with-loading-state.model";
import {error} from "util";

@Injectable()
export class ApplicationDashboardService {
  metrics$: ReplaySubject<PageMetricsDto[]> = new ReplaySubject<PageMetricsDto[]>(1);
  csiValues$: ReplaySubject<ResponseWithLoadingState<ApplicationCsiListDTO>> = new ReplaySubject(1);
  pageCsis$: ReplaySubject<ResponseWithLoadingState<PageCsiDto[]>> = new ReplaySubject(1);
  activeOrRecentlyMeasured$ = new ReplaySubject<ApplicationDTO[]>(1);

  selectedApplication$ = new Subject<ApplicationDTO>();

  constructor(private http: HttpClient) {
    this.updateActiveOrRecentlyMeasured();
    this.selectedApplication$.pipe(
      switchMap((application: ApplicationDTO) => this.updateMetricsForPages(application))
    ).subscribe(this.metrics$);

    this.selectedApplication$.pipe(
      switchMap((application: ApplicationDTO) => this.updateCsiForApplication(application))
    ).subscribe(this.csiValues$);

    this.selectedApplication$.pipe(
      switchMap((application: ApplicationDTO) => this.updateCsiForPages(application))
    ).subscribe(this.pageCsis$);
  }

  updateActiveOrRecentlyMeasured() {
    this.http.get<ApplicationDTO[]>("/applicationDashboard/rest/getAllActiveAndAllRecent")
      .subscribe(next => this.activeOrRecentlyMeasured$.next(next), error => this.handleError(error));
  }

  updateApplicationData(application: ApplicationDTO) {
    this.selectedApplication$.next(application);
  }

  private updateMetricsForPages(applicationDto: ApplicationDTO): Observable<PageMetricsDto[]> {
    const params = this.createParams(applicationDto.id);
    this.metrics$.next(null);
    return this.http.get<PageMetricsDto[]>('/applicationDashboard/rest/getMetricsForApplication', {params}).pipe(
      catchError((error) => {
        this.handleError(error);
        return EMPTY;
      })
    );
  }

  private updateCsiForApplication(applicationDto: ApplicationDTO): Observable<ResponseWithLoadingState<ApplicationCsiListDTO>> {
    this.csiValues$.next({
      data: {
        csiDtoList: [{csiDocComplete: 0, csiVisComplete: 0, date: null}],
        hasCsiConfiguration: false,
        hasInvalidJobResults: false,
        hasJobResults: false,
      },
      isLoading: true
    });
    const params = this.createParams(applicationDto.id);
    return this.http.get<ApplicationCsiListDTO>('/applicationDashboard/rest/getCsiValuesForApplication', {params}).pipe(
      map(dto => <ResponseWithLoadingState<ApplicationCsiListDTO>>{isLoading: false, data: dto}),
      catchError((error) => {
        this.handleError(error);
        return EMPTY;
      })
    );
  }

  private updateCsiForPages(applicationDto: ApplicationDTO): Observable<ResponseWithLoadingState<PageCsiDto[]>> {
    this.pageCsis$.next({data: [], isLoading: true});
    const params = this.createParams(applicationDto.id);
    return this.http.get<PageCsiDto[]>('/applicationDashboard/rest/getCsiValuesForPages', {params: params}).pipe(
      map(dto => <ResponseWithLoadingState<PageCsiDto[]>>{isLoading: false, data: dto}),
      catchError((error) => {
        this.handleError(error);
        return EMPTY;
      })
    );
  }

  private handleError(error: any) {
    console.log(error);
  }

  private createParams(applicationId: number) {
    return {
      applicationId: applicationId ? applicationId.toString() : ""
    };
  }

  createCsiConfiguration(applicationDto: ApplicationDTO) {
    return this.http.post('/applicationDashboard/rest/createCsiConfiguration', {applicationId: applicationDto.id})
      .subscribe((res: any) => {
        window.location.href = '/csiConfiguration/configurations/' + res.csiConfigurationId
      }, catchError((error) => {
        this.handleError(error);
        return EMPTY;
      }));
  }

}
