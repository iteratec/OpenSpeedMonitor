import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, combineLatest, concat, EMPTY, Observable, OperatorFunction, ReplaySubject} from "rxjs";
import {PageMetricsDto} from "../modules/application-dashboard/models/page-metrics.model";
import {PageCsiDto} from "../modules/application-dashboard/models/page-csi.model";
import {ApplicationCsi, ApplicationCsiById, ApplicationCsiDTO, ApplicationCsiDTOById} from "../models/csi-list.model";
import {Application, ApplicationDTO} from "../models/application.model";
import {catchError, distinctUntilKeyChanged, filter, map, startWith, switchMap, take, withLatestFrom} from "rxjs/operators";
import {ResponseWithLoadingState} from "../models/response-with-loading-state.model";
import {Csi, CsiDTO} from "../models/csi.model";


@Injectable()
export class ApplicationService {
  metrics$: ReplaySubject<PageMetricsDto[]> = new ReplaySubject<PageMetricsDto[]>(1);
  applicationCsiById$: BehaviorSubject<ApplicationCsiById> = new BehaviorSubject({});
  pageCsis$: ReplaySubject<ResponseWithLoadingState<PageCsiDto[]>> = new ReplaySubject(1);
  applications$ = new ReplaySubject<Application[]>(1);

  selectedApplication$ = new ReplaySubject<Application>(1);

  constructor(private http: HttpClient) {
    this.loadApplications();
    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.updateMetricsForPages(application))
    ).subscribe(this.metrics$);

    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.updateCsiForApplication(application))
    ).subscribe(this.applicationCsiById$);

    this.selectSelectedApplicationCsi().pipe(
      filter(applicationCsi => !applicationCsi.isLoading),
      withLatestFrom(this.selectedApplication$, (_, application) => application),
      distinctUntilKeyChanged("id"),
      switchMap((application: Application) => this.updateCsiForPages(application))
    ).subscribe(this.pageCsis$);
  }

  loadApplications() {
    this.http.get<ApplicationDTO[]>("/applicationDashboard/rest/getApplications").pipe(
      map(dtos => dtos.map(dto => new Application(dto))),
      map(applications => this.sortApplicationsByName(applications)),
      handleError()
    ).subscribe(next => this.applications$.next(next));
  }

  loadRecentCsiForApplications() {
    const loadedApplicationCsiById =
      this.http.get<ApplicationCsiDTOById>("/applicationDashboard/rest/getCsiValuesForApplications").pipe(
        map(dto => this.mergeApplicationCsiById(this.applicationCsiById$.getValue(), dto)),
        handleError()
      );
    const loadingState = this.applications$.pipe(
      take(1),
      map(applications => this.setLoadingStateForApplicationCsi(this.applicationCsiById$.getValue(), applications))
    );
    concat(loadingState, loadedApplicationCsiById).subscribe(next => this.applicationCsiById$.next(next));
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
      map(dto => ({
        ...this.applicationCsiById$.getValue(),
        [applicationDto.id]: new ApplicationCsi(dto)
      })),
      handleError(),
      startWith({
        ...this.applicationCsiById$.getValue(),
        [applicationDto.id]: new ApplicationCsi({isLoading: true})
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
        csiValues: this.mergeCsiList(applicationCsi.csiValues, updates[applicationId].csiValues || []),
        isLoading: false
      });
      return newState;
    }, {...state});
  }

  private setLoadingStateForApplicationCsi(state: ApplicationCsiById, applications: Application[]) {
    return applications.reduce((newState, application) => {
      const applicationCsi = state[application.id] || {};
      newState[application.id] = new ApplicationCsi({...applicationCsi, isLoading: true});
      return newState;
    }, {...state});
  }

  private mergeCsiList(csiValues: Csi[], updateDtos: CsiDTO[]): Csi[] {
    const updates = updateDtos.map(dto => new Csi(dto));
    return [
      ...csiValues.filter(value => updates.find(update => update.date.getTime() == value.date.getTime())),
      ...updates
    ].sort((a, b) => a.date.getTime() - b.date.getTime());
  }
}

function handleError(): OperatorFunction<any, any> {
  return catchError((error) => {
    console.log(error);
    return EMPTY;
  });
}
