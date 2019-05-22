import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {BehaviorSubject, combineLatest, EMPTY, Observable, OperatorFunction, ReplaySubject} from "rxjs";
import {MeasurandGroup, SelectableMeasurand} from "../../../models/measurand.model";
import {ResponseWithLoadingState} from "../../../models/response-with-loading-state.model";
import {catchError, map, switchMap, startWith} from "rxjs/operators";
import {Application, SelectableApplication, ApplicationWithPages} from "../../../models/application.model";
import {Page} from "../../../models/page.model";
import {Caller, ResultSelectionCommand} from "../models/result-selection-command.model";
import {Chart} from "../models/chart.model";
import {Connectivity} from 'src/app/models/connectivity.model';
import {Location} from 'src/app/models/location.model';
import {MeasuredEvent} from 'src/app/models/measured-event.model';

export enum URL {
  APPLICATIONS = '/resultSelection/getJobGroups',
  APPLICATIONS_AND_PAGES = '/jobGroup/getJobGroupsWithPages',
  EVENTS_AND_PAGES = '/resultSelection/getMeasuredEvents',
  LOCATIONS_AND_BROWSERS = '/resultSelection/getLocations',
  CONNECTIVITIES = '/resultSelection/getConnectivityProfiles',
  RESULT_COUNT = '/resultSelection/getResultCount',
  USER_TIMINGS = '/resultSelection/getUserTimings',
  HERO_TIMINGS = '/resultSelection/getHeroTimings'
}

@Injectable()
export class ResultSelectionService {
  loadTimes$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  userTimings$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  heroTimings$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  requestCounts$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  requestSizes$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  percentages$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);

  selectedApplications$: ReplaySubject<Application[]> = new ReplaySubject<Application[]>(1);
  selectedPages$: ReplaySubject<Page[]> = new ReplaySubject<Page[]>(1);

  applications$: ReplaySubject<SelectableApplication[]> = new ReplaySubject(1);
  applicationsAndPages$: ReplaySubject<ApplicationWithPages[]> = new ReplaySubject(1);
  eventsAndPages$: BehaviorSubject<MeasuredEvent[]> = new BehaviorSubject([]);
  locationsAndBrowsers$: BehaviorSubject<Location[]> = new BehaviorSubject([]);
  connectivities$: ReplaySubject<Connectivity[]> = new ReplaySubject(1);
  resultCount$: ReplaySubject<string> = new ReplaySubject<string>(1);

  constructor(private http: HttpClient) {
    this.getMeasurands();

    this.combinedParams().pipe(
      switchMap(params => this.getUserTimings(params))
    ).subscribe(this.userTimings$);

    this.combinedParams().pipe(
      switchMap(params => this.getHeroTimings(params))
    ).subscribe(this.heroTimings$)
  }

  updateApplications(applications: Application[]) {
    this.selectedApplications$.next(applications);
    this.updatePages([]);
  }

  updatePages(pages: Page[]) {
    this.selectedPages$.next(pages);
  }

  private combinedParams(): Observable<any> {
    return combineLatest(
      this.selectedApplications$,
      this.selectedPages$,
      (applications: Application[], pages: Page[]) => this.createParamsForPerformanceAspect(applications, pages));
  }

  private createParamsForPerformanceAspect(applications: Application[], pages: Page[]) {
    let now: Date = new Date();
    let threeDaysAgo: Date = new Date();
    threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);
    return {
      jobGroupIds: applications.map(app => app.id),
      pageIds: pages.map(page => page.id),
      from: threeDaysAgo.toISOString(),
      to: now.toISOString()
    }
  }

  private getUserTimings(params): Observable<ResponseWithLoadingState<MeasurandGroup>> {
    const userTimingsUrl: string = '/resultSelection/getUserTimings';
    const groupName: string = "USER_TIMINGS";
    this.setToLoading(this.userTimings$, groupName);
    return this.getSelectableMeasurands(userTimingsUrl, params, groupName);
  }

  private getHeroTimings(params): Observable<ResponseWithLoadingState<MeasurandGroup>> {
    const heroTimingsUrl: string = '/resultSelection/getHeroTimings';
    const groupName: string = "HERO_TIMINGS";
    this.setToLoading(this.heroTimings$, groupName);
    return this.getSelectableMeasurands(heroTimingsUrl, params, groupName);
  }

  private getMeasurands() {
    this.setToLoading(this.loadTimes$, "LOAD_TIMES");
    this.setToLoading(this.requestCounts$, "REQUEST_COUNTS");
    this.setToLoading(this.requestSizes$, "REQUEST_SIZES");
    this.setToLoading(this.percentages$, "PERCENTAGES");
    const url: string = '/resultSelection/getMeasurands';
    return this.http.get<MeasurandGroup[]>(url).pipe(
      handleError()
    ).subscribe((groups: MeasurandGroup[]) => {
      groups.forEach((group: MeasurandGroup) => {
        let responseWithLoadingState: ResponseWithLoadingState<MeasurandGroup> = {
          isLoading: false,
          data: {
            name: "frontend.de.iteratec.isr.measurand.group." + group.name,
            values: group.values.map(measurand => ({
              name: "frontend.de.iteratec.isr.measurand." + measurand.name,
              id: measurand.id
            }))
          }
        };
        let concerningSubject$ = this.getDefaultSubjectByMeasurandGroup(group.name);
        if (concerningSubject$) {
          concerningSubject$.next(responseWithLoadingState);
        }
      });
    })
  }




  updateMeasurands(resultSelectionCommand: ResultSelectionCommand): Observable<MeasurandGroup[]> {
    const params = this.createParamsFromResultSelectionCommand(resultSelectionCommand);
    return this.http.get('/resultSelection/getMeasurands', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

   getDefaultSubjectByMeasurandGroup(name: string): ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> | undefined {
    let subject$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>>;
    switch (name) {
      case "LOAD_TIMES":
        subject$ = this.loadTimes$;
        break;
      case "REQUEST_COUNTS":
        subject$ = this.requestCounts$;
        break;
      case "REQUEST_SIZES":
        subject$ = this.requestSizes$;
        break;
      case "PERCENTAGES":
        subject$ = this.percentages$;
        break;
    }
    return subject$;
  }

  private setToLoading(subject$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>>, groupName: string) {
    subject$.next({isLoading: true, data: {name: groupName, values: []}});
  }

  private getSelectableMeasurands(url: string, params: any, groupName: string): Observable<ResponseWithLoadingState<MeasurandGroup>> {
    return this.http.get<SelectableMeasurand[]>(url, {params}).pipe(
      handleError(),
      map(dtos => ({
        isLoading: false,
        data: {name: "frontend.de.iteratec.isr.measurand.group." + groupName, values: dtos}
      })),
    )
  }

  loadSelectableData(resultSelectionCommand: ResultSelectionCommand, chart: Chart): void {
    this.loadResultCount(resultSelectionCommand);
    this.loadUserTimings(resultSelectionCommand);
    this.loadHeroTimings(resultSelectionCommand);

    if (chart !== Chart.PageComparison) {
      this.loadSelectableApplications(resultSelectionCommand);
    } else {
      this.loadSelectableApplicationsAndPages(resultSelectionCommand);
    }

    if (chart === Chart.TimeSeries) {
      this.loadSelectableLocationsAndBrowsers(resultSelectionCommand);
      this.loadSelectableConnectivities(resultSelectionCommand);
    }

    if (chart === Chart.TimeSeries || chart === Chart.PageAggregation || chart === Chart.Distribution) {
      this.loadSelectableEventsAndPages(resultSelectionCommand);
    }
  }

  loadSelectableApplications(resultSelectionCommand: ResultSelectionCommand): void {
    this.fetchResultSelectionData<SelectableApplication[]>(resultSelectionCommand, URL.APPLICATIONS)
      .subscribe(next => this.applications$.next(next));
  }

  loadSelectableApplicationsAndPages(resultSelectionCommand: ResultSelectionCommand): void {
    this.fetchResultSelectionData<ApplicationWithPages[]>(resultSelectionCommand, URL.APPLICATIONS_AND_PAGES)
      .subscribe(next => this.applicationsAndPages$.next(next));
  }

  loadSelectableEventsAndPages(resultSelectionCommand: ResultSelectionCommand): void {
    this.fetchResultSelectionData<MeasuredEvent[]>(resultSelectionCommand, URL.EVENTS_AND_PAGES)
      .subscribe(next => this.eventsAndPages$.next(next));
  }

  loadSelectableLocationsAndBrowsers(resultSelectionCommand: ResultSelectionCommand): void {
    this.fetchResultSelectionData<Location[]>(resultSelectionCommand, URL.LOCATIONS_AND_BROWSERS)
      .subscribe(next => this.locationsAndBrowsers$.next(next));
  }

  loadSelectableConnectivities(resultSelectionCommand: ResultSelectionCommand): void {
    this.fetchResultSelectionData<Connectivity[]>(resultSelectionCommand, URL.CONNECTIVITIES)
      .subscribe(next => this.connectivities$.next(next));
  }

  loadResultCount(resultSelectionCommand: ResultSelectionCommand): void {
    this.fetchResultSelectionData<string>(resultSelectionCommand, URL.RESULT_COUNT)
      .subscribe(next => this.resultCount$.next(next));
  }

  loadUserTimings(resultSelectionCommand: ResultSelectionCommand): void {
    this.fetchResultSelectionData<SelectableMeasurand[]>(resultSelectionCommand, URL.USER_TIMINGS)
      .subscribe(next => {
        const groupName: string = "User Timings";
        let responseWithLoadingState: ResponseWithLoadingState<MeasurandGroup> = {
          isLoading: false,
          data: {name: groupName, values: next}
        };
        this.userTimings$.next(responseWithLoadingState);
      });
  }

  loadHeroTimings(resultSelectionCommand: ResultSelectionCommand): void {
    this.fetchResultSelectionData<SelectableMeasurand[]>(resultSelectionCommand, URL.HERO_TIMINGS)
      .subscribe(next => {
        const groupName: string = "Hero Timings";
        let responseWithLoadingState: ResponseWithLoadingState<MeasurandGroup> = {
          isLoading: false,
          data: {name: groupName, values: next}
        };
        this.heroTimings$.next(responseWithLoadingState);
      });
  }

  fetchResultSelectionData<T>(resultSelectionCommand: ResultSelectionCommand, url: URL): Observable<T> {
    const params = this.createParamsFromResultSelectionCommand(resultSelectionCommand);
    return this.http.get<T>(url, {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  private createParamsFromResultSelectionCommand(resultSelectionCommand: ResultSelectionCommand) {
    let params = new HttpParams()
      .set('from', resultSelectionCommand.from.toISOString())
      .set('to', resultSelectionCommand.to.toISOString())
      .set('caller', Caller[resultSelectionCommand.caller]);

    Object.keys(resultSelectionCommand).forEach(key => {
      if (key === 'from' || key === 'to' || key === 'caller') {
        return;
      }
      resultSelectionCommand[key].forEach(id => {
        params = params.append(key, id.toString())
      })
    });

    return params;
  }

}


function handleError(): OperatorFunction<any, any> {
  return catchError((error) => {
    console.log(error);
    return EMPTY;
  });
}
