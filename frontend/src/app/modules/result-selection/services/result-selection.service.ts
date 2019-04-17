import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {combineLatest, EMPTY, Observable, OperatorFunction, ReplaySubject} from "rxjs";
import {MeasurandGroup, SelectableMeasurand} from "../../../models/measurand.model";
import {ResponseWithLoadingState} from "../../../models/response-with-loading-state.model";
import {catchError, map, switchMap, startWith} from "rxjs/operators";
import {Application, SelectableApplication, ApplicationWithPages} from "../../../models/application.model";
import {Page} from "../../../models/page.model";
import {Caller, ResultSelectionCommand} from "../models/result-selection-command.model";
//import {SelectableHeroTiming} from "../models/selectable-hero-timing.model";
//import {SelectableUserTiming} from "../models/selectable-user-timing.model";
import {Chart} from "../models/chart.model";
import {Connectivity} from 'src/app/models/connectivity.model';
import {Location} from 'src/app/models/location.model';
import {MeasuredEvent} from 'src/app/models/measured-event.model';

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

  // TODO
  applications$: ReplaySubject<SelectableApplication[]> = new ReplaySubject(1);
  applicationsAndPages$: ReplaySubject<ApplicationWithPages[]> = new ReplaySubject(1);
  eventsAndPages$: ReplaySubject<MeasuredEvent[]> = new ReplaySubject(1);
  locationsAndBrowsers$: ReplaySubject<Location[]> = new ReplaySubject(1);
  connectivities$: ReplaySubject<Connectivity[]> = new ReplaySubject(1);
  //selectableHeroTimings$: ReplaySubject<SelectableHeroTiming[]> = new ReplaySubject<SelectableHeroTiming[]>(1);
  //selectableUserTimings$: ReplaySubject<SelectableUserTiming[]> = new ReplaySubject<SelectableUserTiming[]>(1);
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
      (applications: Application[], pages: Page[]) => this.generateParams(applications, pages));
  }

  private generateParams(applications: Application[], pages: Page[]) {
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

  private getDefaultSubjectByMeasurandGroup(name: string): ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> | undefined {
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
    //this.loadSelectableUserTimings(resultSelectionCommand);
    //this.loadSelectableHeroTimings(resultSelectionCommand);

    if(chart !== Chart.PageComparison) {
      this.loadSelectableApplications(resultSelectionCommand);
    } else {
      this.loadSelectableApplicationsAndPages(resultSelectionCommand);
    }

    if (chart === Chart.TimeSeries) {
      this.loadSelectableLocationsAndBrowsers(resultSelectionCommand);
      this.loadSelectableConnectivities(resultSelectionCommand);
    }

    if(chart === Chart.TimeSeries || chart === Chart.PageAggregation || chart === Chart.Distribution) {
      this.loadSelectableEventsAndPages(resultSelectionCommand);
    }

  }

  loadSelectableApplications(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableApplications(resultSelectionCommand).subscribe(next => this.applications$.next(next));
  }

  loadSelectableApplicationsAndPages(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableApplicationsAndPages(resultSelectionCommand).subscribe(next => this.applicationsAndPages$.next(next));
  }

  loadSelectableEventsAndPages(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableEventsAndPages(resultSelectionCommand).subscribe(next => this.eventsAndPages$.next(next));
  }

  loadSelectableLocationsAndBrowsers(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableLocationsAndBrowsers(resultSelectionCommand).subscribe(next => this.locationsAndBrowsers$.next(next));
  }

  loadSelectableConnectivities(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableConnectivities(resultSelectionCommand).subscribe(next => this.connectivities$.next(next));
  }

  /*loadSelectableUserTimings(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableUserTimings(resultSelectionCommand).subscribe(next => this.selectableUserTimings$.next(next));
  }

  loadSelectableHeroTimings(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableHeroTimings(resultSelectionCommand).subscribe(next => this.selectableHeroTimings$.next(next));
  }
*/
  loadResultCount(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateResultCount(resultSelectionCommand).subscribe(next => this.resultCount$.next(next));
  }

  updateSelectableApplications(resultSelectionCommand: ResultSelectionCommand): Observable<SelectableApplication[]> {
    const params = this.createParams(resultSelectionCommand);
    //console.log(params);
    return this.http.get<SelectableApplication[]>('/resultSelection/getJobGroups', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  updateSelectableApplicationsAndPages(resultSelectionCommand: ResultSelectionCommand): Observable<ApplicationWithPages[]> {
    const params = this.createParams(resultSelectionCommand);
    return this.http.get<ApplicationWithPages[]>('/jobGroup/getJobGroupsWithPages', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  updateSelectableEventsAndPages(resultSelectionCommand: ResultSelectionCommand): Observable<MeasuredEvent[]> {
    const params = this.createParams(resultSelectionCommand);
    return this.http.get<MeasuredEvent[]>('/resultSelection/getMeasuredEvents', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  updateSelectableLocationsAndBrowsers(resultSelectionCommand: ResultSelectionCommand): Observable<Location[]> {
    const params = this.createParams(resultSelectionCommand);
    return this.http.get<Location[]>('/resultSelection/getLocations', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  updateSelectableConnectivities(resultSelectionCommand: ResultSelectionCommand): Observable<Connectivity[]> {
    const params = this.createParams(resultSelectionCommand);
    return this.http.get('/resultSelection/getConnectivityProfiles', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  /*updateSelectableUserTimings(resultSelectionCommand: ResultSelectionCommand): Observable<SelectableUserTiming[]> {
    const params = this.createParams(resultSelectionCommand);
    return this.http.get('/resultSelection/getUserTimings', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  updateSelectableHeroTimings(resultSelectionCommand: ResultSelectionCommand): Observable<SelectableHeroTiming[]> {
    const params = this.createParams(resultSelectionCommand);
    return this.http.get('/resultSelection/getHeroTimings', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }
*/
  updateResultCount(resultSelectionCommand: ResultSelectionCommand): Observable<string> {
    const params = this.createParams(resultSelectionCommand);
    return this.http.get('/resultSelection/getResultCount', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  
  private createParams(resultSelectionCommand: ResultSelectionCommand) {
    return {
      from: resultSelectionCommand.from.toISOString(),
      to: resultSelectionCommand.to.toISOString(),
      caller: Caller[resultSelectionCommand.caller],
      ...(resultSelectionCommand.jobGroupIds.length && { jobGroupIds: resultSelectionCommand.jobGroupIds.toString() }),
      ...(resultSelectionCommand.pageIds.length && { pageIds: resultSelectionCommand.pageIds.toString() }),
      ...(resultSelectionCommand.measuredEventIds.length && { measuredEventIds: resultSelectionCommand.measuredEventIds.toString() }),
      ...(resultSelectionCommand.browserIds.length && { browserIds: resultSelectionCommand.browserIds.toString() }),
      ...(resultSelectionCommand.locationIds.length && { locationIds: resultSelectionCommand.locationIds.toString() }),
      ...(resultSelectionCommand.selectedConnectivities.length && { selectedConnectivities: resultSelectionCommand.selectedConnectivities })
    }
  }
}

function handleError(): OperatorFunction<any, any> {
  return catchError((error) => {
    console.log(error);
    return EMPTY;
  });
}
