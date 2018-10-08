import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, EMPTY, Observable, OperatorFunction, ReplaySubject, Subject} from "rxjs/index";
import {PageMetricsDto} from "../modules/application-dashboard/models/page-metrics.model";
import {PageCsiDto} from "../modules/application-dashboard/models/page-csi.model";
import {ApplicationCsi, ApplicationCsiById, ApplicationCsiDTO} from "../models/csi-list.model";
import {Application, ApplicationDTO} from "../models/application.model";
import {catchError, map, switchMap} from "rxjs/internal/operators";
import {ResponseWithLoadingState} from "../models/response-with-loading-state.model";


@Injectable()
export class ApplicationService {
  metrics$: ReplaySubject<PageMetricsDto[]> = new ReplaySubject<PageMetricsDto[]>(1);
  applicationCsiById$: BehaviorSubject<ApplicationCsiById> = new BehaviorSubject({});
  pageCsis$: ReplaySubject<ResponseWithLoadingState<PageCsiDto[]>> = new ReplaySubject(1);
  applications$ = new ReplaySubject<Application[]>(1);

  selectedApplication$ = new Subject<Application>();

  constructor(private http: HttpClient) {
    this.loadApplications();
    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.updateMetricsForPages(application))
    ).subscribe(this.metrics$);

    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.updateCsiForApplication(application))
    ).subscribe(this.applicationCsiById$);

    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.updateCsiForPages(application))
    ).subscribe(this.pageCsis$);
  }

  loadApplications() {
    this.http.get<ApplicationDTO[]>("/applicationDashboard/rest/getAllActiveAndAllRecent").pipe(
      map(dtos => dtos.map(dto => new Application(dto))),
      map(applications => this.sortApplicationsByName(applications)),
      handleError()
    ).subscribe(next => this.applications$.next(next));
  }

  updateSelectedApplication(application: Application) {
    this.selectedApplication$.next(application);
  }

  selectApplicationCsi(applicationId: number) {
    return this.applicationCsiById$.pipe(
      map(state => state[applicationId]),
    );
  }

  selectSelectedApplicationCsi() {
    return this.selectedApplication$.pipe(
      switchMap(selectedApplication => this.selectApplicationCsi(selectedApplication.id))
    );
  }

  private updateMetricsForPages(applicationDto: Application): Observable<PageMetricsDto[]> {
    const params = this.createParams(applicationDto.id);
    this.metrics$.next(null);
    return this.http.get<PageMetricsDto[]>('/applicationDashboard/rest/getMetricsForApplication', {params}).pipe(
      handleError()
    );
  }

  private updateCsiForApplication(applicationDto: Application): Observable<ApplicationCsiById> {
    this.applicationCsiById$.next({
      ...this.applicationCsiById$.getValue(),
      [applicationDto.id]: new ApplicationCsi({isLoading: true})
    });
    const params = this.createParams(applicationDto.id);
    return this.http.get<ApplicationCsiDTO>('/applicationDashboard/rest/getCsiValuesForApplication', {params}).pipe(
      map(dto => new ApplicationCsi(dto)),
      handleError()
    );
  }

  private updateCsiForPages(applicationDto: Application): Observable<ResponseWithLoadingState<PageCsiDto[]>> {
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
