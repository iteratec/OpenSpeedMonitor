import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
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
import {catchError, distinctUntilKeyChanged, filter, map, startWith, switchMap, withLatestFrom} from "rxjs/operators";
import {ResponseWithLoadingState} from "../models/response-with-loading-state.model";
import {Csi, CsiDTO} from "../models/csi.model";
import {FailingJobStatistic} from "../modules/application-dashboard/models/failing-job-statistic.model";
import {GraphiteServer, GraphiteServerDTO} from "../modules/application-dashboard/models/graphite-server.model";
import {FailingJob, FailingJobDTO} from '../modules/landing/models/failing-jobs.model';
import {ResultSelectionService} from "./result-selection.service";
import {PerformanceAspect} from "../models/perfomance-aspect.model";
import {Page} from "../models/page.model";

@Injectable()
export class ApplicationService {
  metrics$: ReplaySubject<PageMetricsDto[]> = new ReplaySubject<PageMetricsDto[]>(1);
  applicationCsiById$: BehaviorSubject<ApplicationCsiById> = new BehaviorSubject({isLoading: false});
  pageCsis$: ReplaySubject<ResponseWithLoadingState<PageCsiDto[]>> = new ReplaySubject(1);
  applications$ = new BehaviorSubject<ResponseWithLoadingState<Application[]>>({isLoading: false, data: null});
  failingJobStatistics$: ReplaySubject<FailingJobStatistic> = new ReplaySubject<FailingJobStatistic>(1);
  failingJobs$: ReplaySubject<{}> = new ReplaySubject<{}>(1);
  jobHealthGraphiteServers$: ReplaySubject<GraphiteServer[]> = new ReplaySubject<GraphiteServer[]>(1);
  availableGraphiteServers$: ReplaySubject<GraphiteServer[]> = new ReplaySubject<GraphiteServer[]>(1);
  performanceAspectForPage$: BehaviorSubject<ResponseWithLoadingState<PerformanceAspect>[]> = new BehaviorSubject([]);

  selectedPage$: ReplaySubject<Page> = new ReplaySubject<Page>(1);
  selectedApplication$ = new ReplaySubject<Application>(1);

  constructor(private http: HttpClient, private measurandsService: ResultSelectionService) {
    this.combinedParams().pipe(
      switchMap(params => this.getPerformanceAspects(params))
    ).subscribe(this.performanceAspectForPage$);

    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.updateMetricsForPages(application))
    ).subscribe(this.metrics$);

    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.updateCsiForApplication(application)),
    ).subscribe(this.applicationCsiById$);

    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.updateFailingJobStatistics(application))
    ).subscribe(this.failingJobStatistics$);

    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.updateActiveJobHealthGraphiteServers(application))
    ).subscribe(this.jobHealthGraphiteServers$);

    this.selectedApplication$.pipe(
      switchMap((application: Application) => this.updateAvailableGraphiteServers(application))
    ).subscribe(this.availableGraphiteServers$);

    this.selectSelectedApplicationCsi().pipe(
      withLatestFrom(this.selectedApplication$, (_, application) => application),
      distinctUntilKeyChanged("id"),
      switchMap((application: Application) => this.updateCsiForPages(application))
    ).subscribe(this.pageCsis$);

    this.getFailingJobs().pipe(
      map(failingJobs => {
          return this.reduceFailingJobs(failingJobs);
        }
      )
    ).subscribe(next => this.failingJobs$.next(next));
  }

  private reduceFailingJobs(failingJobs) {
    if (!failingJobs) {
      return null;
    }

    return failingJobs.reduce((failingJobsByApplication, currentValue) => {
      if (!failingJobsByApplication[currentValue.application]) {
        failingJobsByApplication[currentValue.application] = [];
      }
      failingJobsByApplication[currentValue.application].push(currentValue);
      return failingJobsByApplication;
    }, {});
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

  loadAvailableGraphiteServers(application: Application) {
    this.updateAvailableGraphiteServers(application).subscribe(next => this.availableGraphiteServers$.next(next));
  }

  loadActiveJobHealthGraphiteServers(application: Application) {
    this.updateActiveJobHealthGraphiteServers(application).subscribe(next => this.jobHealthGraphiteServers$.next(next));
  }

  updateSelectedApplication(application: Application) {
    this.measurandsService.updateApplications([application]);
    this.selectedApplication$.next(application);
  }

  updatePage(page: Page) {
    this.selectedPage$.next(page);
  }

  private combinedParams(): Observable<any> {
    return combineLatest(
      this.selectedApplication$,
      this.selectedPage$,
      (application: Application, page: Page) => this.generateParams(application, page));
  }

  private generateParams(application: Application, page: Page) {
    return {
      applicationId: application.id,
      pageId: page.id
    }
  }

  selectSelectedApplicationCsi(): Observable<ApplicationCsi> {
    return combineLatest(this.selectedApplication$, this.applicationCsiById$).pipe(
      map(([application, csiById]) => csiById[application.id]),
      filter(applicationCsi => !!applicationCsi)
    );
  }

  private getPerformanceAspects(params): Observable<ResponseWithLoadingState<PerformanceAspect>[]> {
    this.performanceAspectForPage$.next([]);
    return this.http.get<Performance[]>('/applicationDashboard/rest/getPerformanceAspectsForApplication', {params}).pipe(
      handleError(),
      map(dtos => dtos.map(dto => ({isLoading: false, data: dto})))
    );
  }

  createOrUpdatePerformanceAspect(perfAspectToCreateOrUpdate: PerformanceAspect) {
    this.replacePerformanceAspect(perfAspectToCreateOrUpdate, true);
    const params = {
      performanceAspectId: perfAspectToCreateOrUpdate.id,
      pageId: perfAspectToCreateOrUpdate.pageId,
      applicationId: perfAspectToCreateOrUpdate.jobGroupId,
      performanceAspectType: perfAspectToCreateOrUpdate.performanceAspectType,
      metricIdentifier: perfAspectToCreateOrUpdate.measurand.id
    };
    this.http.post<PerformanceAspect>('/applicationDashboard/rest/createOrUpdatePerformanceAspect', params).pipe(
      handleError()).subscribe((createdAspect: PerformanceAspect) => this.replacePerformanceAspect(createdAspect, false))
  }


  private replacePerformanceAspect(perfAspectToReplace: PerformanceAspect, isLoading: boolean) {
    let prevValue: ResponseWithLoadingState<PerformanceAspect>[] = this.performanceAspectForPage$.getValue();
    let existingAspect: ResponseWithLoadingState<PerformanceAspect> = prevValue.find((exAspect: ResponseWithLoadingState<PerformanceAspect>) => {
      return exAspect.data.id == perfAspectToReplace.id &&
        exAspect.data.performanceAspectType == perfAspectToReplace.performanceAspectType &&
        exAspect.data.pageId == perfAspectToReplace.pageId &&
        exAspect.data.jobGroupId == perfAspectToReplace.jobGroupId
    });
    if (existingAspect) {
      prevValue = this.performanceAspectForPage$.getValue();
      prevValue[prevValue.indexOf(existingAspect)] = {isLoading: isLoading, data: perfAspectToReplace};
      this.performanceAspectForPage$.next(prevValue);
    }
  }

  private updateMetricsForPages(applicationDto: Application): Observable<PageMetricsDto[]> {
    this.pageCsis$.next({data: [], isLoading: true});
    this.metrics$.next(null);
    const params = this.createParams(applicationDto.id);
    return this.http.get<PageMetricsDto[]>('/applicationDashboard/rest/getMetricsForApplication', {params}).pipe(
      handleError()
    )
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

  removeAndAddJobHealthGraphiteServers(application: Application, add: GraphiteServer[], remove: GraphiteServer[]) {
    return this.sendRemoveJobHealthGraphiteServersRequest(application, remove).subscribe((res: any) => {
      if (res.removed === true) {
        this.saveJobHealthGraphiteServers(application, add);
      }
    })
  }

  saveJobHealthGraphiteServers(application: Application, graphiteServers: GraphiteServer[]) {
    return this.sendAddJobHealthGraphiteServersRequest(application, graphiteServers)
      .subscribe((res: any) => {
        if (res.added === true) {
          this.loadAvailableGraphiteServers(application);
          this.loadActiveJobHealthGraphiteServers(application);
        }
      })
  }

  removeJobHealthGraphiteServers(application: Application, graphiteServers: GraphiteServer[]) {
    return this.sendRemoveJobHealthGraphiteServersRequest(application, graphiteServers).subscribe((res: any) => {
      if (res.removed === true) {
        this.loadAvailableGraphiteServers(application);
        this.loadActiveJobHealthGraphiteServers(application);
      }
    });
  }

  private sendAddJobHealthGraphiteServersRequest(application: Application, graphiteServers: GraphiteServer[]) {
    const graphiteServerIds = graphiteServers.map(value => value.id);
    return this.http.post('/applicationDashboard/rest/saveJobHealthGraphiteServers', {
      applicationId: application.id,
      graphiteServerIds: graphiteServerIds
    })
      .pipe(handleError());
  }

  private sendRemoveJobHealthGraphiteServersRequest(application: Application, graphiteServers: GraphiteServer[]) {
    const graphiteServerIds = graphiteServers.map(value => value.id);
    return this.http.post('/applicationDashboard/rest/removeJobHealthGraphiteServers', {
      applicationId: application.id,
      graphiteServerIds: graphiteServerIds
    })
      .pipe(handleError());
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

  updateFailingJobStatistics(application: Application): Observable<FailingJobStatistic> {
    const params = this.createParams(application.id);
    return this.http.get<FailingJobStatistic>('/applicationDashboard/rest/getFailingJobStatistics', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  getFailingJobs(): Observable<FailingJob[]> {
    return this.http.get<FailingJobDTO[]>('/applicationDashboard/rest/getFailingJobs').pipe(
      map(failingJobs => failingJobs.map(dto => new FailingJob(dto))),
      handleError(),
      startWith(null)
    );
  }

  updateActiveJobHealthGraphiteServers(application: Application): Observable<GraphiteServer[]> {
    const params = this.createParams(application.id);
    return this.http.get<GraphiteServer[]>('/applicationDashboard/rest/getActiveJobHealthGraphiteServers', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  updateAvailableGraphiteServers(application: Application): Observable<GraphiteServer[]> {
    const params = this.createParams(application.id);
    return this.http.get<GraphiteServer[]>('/applicationDashboard/rest/getAvailableGraphiteServers', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  createGraphiteServer(server: GraphiteServerDTO): Observable<Map<String, any>> {
    let params = new HttpParams().set("port", server.port.toString());
    params = params.set("address", server.address.toString());
    params = params.set("prefix", server.prefix.toString());
    params = params.set("protocol", server.protocol);
    params = params.set("webAppAddress", server.webAppAddress.toString());
    return this.http.post<GraphiteServerDTO>('/applicationDashboard/rest/createGraphiteServer', params).pipe(
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
