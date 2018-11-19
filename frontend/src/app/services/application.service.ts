import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, combineLatest, EMPTY, Observable, OperatorFunction, ReplaySubject} from "rxjs";
import {PageMetricsDto} from "../modules/application-dashboard/models/page-metrics.model";
import {PageCsiDto} from "../modules/application-dashboard/models/page-csi.model";
import {
  ApplicationCsi,
  ApplicationCsiById,
  ApplicationCsiDTO,
  ApplicationCsiDTOById
} from "../models/application-csi.model";
import {Application, ApplicationDTO} from "../models/application.model";
import {
  catchError,
  distinctUntilKeyChanged,
  filter,
  map,
  startWith,
  switchMap,
  withLatestFrom
} from "rxjs/operators";
import {ResponseWithLoadingState} from "../models/response-with-loading-state.model";
import {Csi, CsiDTO} from "../models/csi.model";
import {
  FailingJobStatistic} from "../modules/application-dashboard/models/failing-job-statistic.model";


@Injectable()
export class ApplicationService {
  metrics$: ReplaySubject<PageMetricsDto[]> = new ReplaySubject<PageMetricsDto[]>(1);
  applicationCsiById$: BehaviorSubject<ApplicationCsiById> = new BehaviorSubject({isLoading: false});
  pageCsis$: ReplaySubject<ResponseWithLoadingState<PageCsiDto[]>> = new ReplaySubject(1);
  applications$ = new BehaviorSubject<ResponseWithLoadingState<Application[]>>({isLoading: false, data: null});
  failingJobStatistics$: ReplaySubject<FailingJobStatistic> = new ReplaySubject<FailingJobStatistic>(1);

  selectedApplication$ = new ReplaySubject<Application>(1);

  constructor(private http: HttpClient) {
    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.updateMetricsForPages(application))
    ).subscribe(this.metrics$);

    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.updateCsiForApplication(application)),
    ).subscribe(this.applicationCsiById$);

    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.loadFailingJobStatistics(application))
    ).subscribe(this.failingJobStatistics$);

    this.selectSelectedApplicationCsi().pipe(
      withLatestFrom(this.selectedApplication$, (_, application) => application),
      distinctUntilKeyChanged("id"),
      switchMap((application: Application) => this.updateCsiForPages(application))
    ).subscribe(this.pageCsis$);
  }

  loadApplications() {
    this.http.get<ApplicationDTO[]>("/applicationDashboard/rest/getApplications").pipe(
      handleError(),
      map(dtos => dtos.map(dto => new Application(dto))),
      map(applications => ({
        isLoading: false,
        data: this.sortApplicationsByName(applications)
      })),
      startWith({
        ...this.applications$.getValue(),
        isLoading: true
      })
    ).subscribe(next => this.applications$.next(next));
  }

  loadRecentCsiForApplications() {
    this.http.get<ApplicationCsiDTOById>("/applicationDashboard/rest/getCsiValuesForApplications").pipe(
      map(dto => this.mergeApplicationCsiById(this.applicationCsiById$.getValue(), dto)),
      handleError(),
      startWith({...this.applicationCsiById$.getValue(), isLoading: true})
    ).subscribe(next => this.applicationCsiById$.next(next));
  }

  updateSelectedApplication(application: Application) {
    this.selectedApplication$.next(application);
  }

  selectSelectedApplicationCsi(): Observable<ApplicationCsi> {
    return combineLatest(this.selectedApplication$, this.applicationCsiById$).pipe(
      map(([application, csiById]) => csiById[application.id]),
      filter(applicationCsi => !!applicationCsi)
    );
  }

  private updateMetricsForPages(applicationDto: Application): Observable<PageMetricsDto[]> {
    this.pageCsis$.next({data: [], isLoading: true});
    this.metrics$.next(null);
    const params = this.createParams(applicationDto.id);
    return this.http.get<PageMetricsDto[]>('/applicationDashboard/rest/getMetricsForApplication', {params}).pipe(
      handleError()
    );
  }

  private updateCsiForApplication(applicationDto: Application): Observable<ApplicationCsiById> {
    const params = this.createParams(applicationDto.id);
    return this.http.get<ApplicationCsiDTO>('/applicationDashboard/rest/getCsiValuesForApplication', {params}).pipe(
      map(dto => this.mergeApplicationCsiById(this.applicationCsiById$.getValue(), {[applicationDto.id]: dto})),
      handleError(),
      startWith({
        ...this.applicationCsiById$.getValue(),
        isLoading: true
      })
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

  private mergeApplicationCsiById(state: ApplicationCsiById, updates: ApplicationCsiDTOById): ApplicationCsiById {
    return Object.keys(updates).reduce((newState, applicationId) => {
      const applicationCsi = state[applicationId] || {};
      newState[applicationId] = new ApplicationCsi({
        ...applicationCsi,
        ...updates[applicationId],
        csiValues: this.mergeCsiList(applicationCsi.csiValues || [], updates[applicationId].csiValues || []),
      });
      return newState;
    }, {...state, isLoading: false});
  }

  private mergeCsiList(csiValues: Csi[], updateDtos: CsiDTO[]): Csi[] {
    const updates = updateDtos.map(dto => new Csi(dto));
    return [
      ...csiValues.filter(value => updates.find(update => update.date.getTime() == value.date.getTime())),
      ...updates
    ].sort((a, b) => a.date.getTime() - b.date.getTime());
  }

  loadFailingJobStatistics(application: Application): Observable<FailingJobStatistic> {
    const params = this.createParams(application.id);
    return this.http.get<FailingJobStatistic>('/applicationDashboard/rest/getFailingJobStatistics', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  loadAllFailingJobStatistics(): Observable<FailingJobStatistic[]> {
    return this.http.get<FailingJobStatistic[]>('/applicationDashboard/rest/getAllFailingJobStatistics').pipe(
      handleError(),
      startWith(null)
    )
  }
}

function handleError(): OperatorFunction<any, any> {
  return catchError((error) => {
    console.log(error);
    return EMPTY;
  });
}
