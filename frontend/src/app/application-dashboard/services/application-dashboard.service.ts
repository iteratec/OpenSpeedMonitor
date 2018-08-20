import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EMPTY, Observable, ReplaySubject, Subject} from "rxjs/index";
import {PageDto} from "../models/page.model";
import {MetricsDto} from "../models/metrics.model";
import {PageCsiDto} from "../models/page-csi.model";
import {ApplicationCsiListDTO} from "../models/csi-list.model";
import {ApplicationDTO} from "../models/application.model";
import {PageCsiResponse} from "../models/page-csi-response.model";
import {catchError, map, switchMap} from "rxjs/internal/operators";

@Injectable()
export class ApplicationDashboardService {
  metrics$: ReplaySubject<MetricsDto[]> = new ReplaySubject<MetricsDto[]>(1);
  pages$: ReplaySubject<PageDto[]> = new ReplaySubject<PageDto[]>(1);
  csiValues$: ReplaySubject<ApplicationCsiListDTO> = new ReplaySubject<ApplicationCsiListDTO>(1);
  pageCsis$: ReplaySubject<PageCsiResponse> = new ReplaySubject<PageCsiResponse>(1);
  activeOrRecentlyMeasured$ = new ReplaySubject<ApplicationDTO[]>(1);

  selectedApplication$ = new Subject<ApplicationDTO>();

  constructor(private http: HttpClient) {
    this.updateActiveOrRecentlyMeasured()
    this.selectedApplication$.pipe(
      switchMap((application: ApplicationDTO) => this.updateMetricsForApplication(application))
    ).subscribe(this.metrics$);

    this.selectedApplication$.pipe(
      switchMap((application: ApplicationDTO) => this.updatePagesForApplication(application))
    ).subscribe(this.pages$);

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

  private updateMetricsForApplication(applicationDto: ApplicationDTO): Observable<MetricsDto[]> {
    const params = this.createParams(applicationDto.id);
    return this.http.get<MetricsDto[]>('/applicationDashboard/rest/getMetricsForApplication', {params}).pipe(
      catchError((error) => {
        this.handleError(error);
        return EMPTY;
      })
    );
  }

  private updatePagesForApplication(applicationDto: ApplicationDTO): Observable<PageDto[]> {
    const params = this.createParams(applicationDto.id);
    return this.http.get<PageDto[]>('/applicationDashboard/rest/getPagesForApplication', {params}).pipe(
      catchError((error) => {
        this.handleError(error);
        return EMPTY;
      })
    );
  }

  private updateCsiForApplication(applicationDto: ApplicationDTO): Observable<ApplicationCsiListDTO> {
    this.csiValues$.next({
      csiDtoList: [{csiDocComplete: 0, csiVisComplete: 0, date: null}],
      hasCsiConfiguration: false,
      isLoading: true
    });
    const params = this.createParams(applicationDto.id);
    return this.http.get<ApplicationCsiListDTO>('/applicationDashboard/rest/getCsiValuesForApplication', {params}).pipe(
      catchError((error) => {
        this.handleError(error);
        return EMPTY;
      })
    );
  }

  private updateCsiForPages(applicationDto: ApplicationDTO): Observable<PageCsiResponse> {
    this.pageCsis$.next({pageCsis: [], isLoading: true});
    const params = this.createParams(applicationDto.id);
    return this.http.get<PageCsiDto[]>('/applicationDashboard/rest/getCsiValuesForPages', {params: params}).pipe(
      map(dto => <PageCsiResponse>{isLoading: false, pageCsis: dto}),
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

}
