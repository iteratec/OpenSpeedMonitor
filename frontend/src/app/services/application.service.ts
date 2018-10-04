import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EMPTY, Observable, OperatorFunction, ReplaySubject, Subject} from "rxjs/index";
import {PageMetricsDto} from "../modules/application-dashboard/models/page-metrics.model";
import {PageCsiDto} from "../modules/application-dashboard/models/page-csi.model";
import {ApplicationCsiListDTO} from "../modules/application-dashboard/models/csi-list.model";
import {Application, ApplicationDTO} from "../models/application.model";
import {catchError, map, switchMap} from "rxjs/internal/operators";
import {ResponseWithLoadingState} from "../modules/application-dashboard/models/response-with-loading-state.model";


@Injectable()
export class ApplicationService {
  metrics$: ReplaySubject<PageMetricsDto[]> = new ReplaySubject<PageMetricsDto[]>(1);
  csiValues$: ReplaySubject<ResponseWithLoadingState<ApplicationCsiListDTO>> = new ReplaySubject(1);
  pageCsis$: ReplaySubject<ResponseWithLoadingState<PageCsiDto[]>> = new ReplaySubject(1);
  applications$ = new ReplaySubject<Application[]>(1);

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
    this.http.get<ApplicationDTO[]>("/applicationDashboard/rest/getAllActiveAndAllRecent").pipe(
      map(dtos => dtos.map(dto => new Application(dto))),
      map(applications => this.sortApplicationsByName(applications)),
      handleError()
    ).subscribe(next => this.applications$.next(next));
  }

  updateApplicationData(application: ApplicationDTO) {
    this.selectedApplication$.next(application);
  }

  private updateMetricsForPages(applicationDto: ApplicationDTO): Observable<PageMetricsDto[]> {
    const params = this.createParams(applicationDto.id);
    this.metrics$.next(null);
    return this.http.get<PageMetricsDto[]>('/applicationDashboard/rest/getMetricsForApplication', {params}).pipe(
      handleError()
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
      handleError()
    );
  }

  private updateCsiForPages(applicationDto: ApplicationDTO): Observable<ResponseWithLoadingState<PageCsiDto[]>> {
    this.pageCsis$.next({data: [], isLoading: true});
    const params = this.createParams(applicationDto.id);
    return this.http.get<PageCsiDto[]>('/applicationDashboard/rest/getCsiValuesForPages', {params: params}).pipe(
      map(dto => <ResponseWithLoadingState<PageCsiDto[]>>{isLoading: false, data: dto}),
      handleError()
    );
  }

  private createParams(applicationId: number) {
    return {
      applicationId: applicationId ? applicationId.toString() : ""
    };
  }

  createCsiConfiguration(applicationDto: ApplicationDTO) {
    return this.http.post('/applicationDashboard/rest/createCsiConfiguration', {applicationId: applicationDto.id})
      .pipe(handleError())
      .subscribe((res: any) => {
        window.location.href = '/csiConfiguration/configurations/' + res.csiConfigurationId
      });
  }

  private sortApplicationsByName(applications: Application[]): Application[] {
    return applications.sort((a, b) => a.name.localeCompare(b.name, [], {sensitivity: 'base'}));
  }

}

function handleError(): OperatorFunction<any, any> {
  return catchError((error) => {
    console.log(error);
    return EMPTY;
  });
}
