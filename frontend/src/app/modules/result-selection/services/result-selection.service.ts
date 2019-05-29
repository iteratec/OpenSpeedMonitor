import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {BehaviorSubject, combineLatest, EMPTY, Observable, OperatorFunction, ReplaySubject} from "rxjs";
import {MeasurandGroup, SelectableMeasurand} from "../../../models/measurand.model";
import {ResponseWithLoadingState} from "../../../models/response-with-loading-state.model";
import {catchError, map, switchMap, startWith} from "rxjs/operators";
import {Application} from "../../../models/application.model";
import {Page} from "../../../models/page.model";
import {Caller, ResultSelectionCommand} from "../models/result-selection-command.model";
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
  loadTimes$: ReplaySubject<MeasurandGroup> = new ReplaySubject(1);
  userTimings$: ReplaySubject<MeasurandGroup> = new ReplaySubject(1);
  heroTimings$: ReplaySubject<MeasurandGroup> = new ReplaySubject(1);
  requestCounts$: ReplaySubject<MeasurandGroup> = new ReplaySubject(1);
  requestSizes$: ReplaySubject<MeasurandGroup> = new ReplaySubject(1);
  percentages$: ReplaySubject<MeasurandGroup> = new ReplaySubject(1);

  selectedApplications$: ReplaySubject<Application[]> = new ReplaySubject<Application[]>(1);
  selectedPages$: ReplaySubject<Page[]> = new ReplaySubject<Page[]>(1);

  eventsAndPages$: BehaviorSubject<MeasuredEvent[]> = new BehaviorSubject([]);
  locationsAndBrowsers$: BehaviorSubject<Location[]> = new BehaviorSubject([]);
  connectivities$: ReplaySubject<Connectivity[]> = new ReplaySubject(1);

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

  private getUserTimings(params): Observable<MeasurandGroup> {
    const userTimingsUrl: string = '/resultSelection/getUserTimings';
    const groupName: string = "USER_TIMINGS";
    this.setToLoading(this.userTimings$, groupName);
    return this.getSelectableMeasurands(userTimingsUrl, params, groupName);
  }

  private getHeroTimings(params): Observable<MeasurandGroup> {
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
        let responseWithLoadingState: MeasurandGroup = {
          isLoading: false,
          name: "frontend.de.iteratec.isr.measurand.group." + group.name,
          values: group.values.map(measurand => ({
            name: "frontend.de.iteratec.isr.measurand." + measurand.name,
            id: measurand.id
          }))
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

   getDefaultSubjectByMeasurandGroup(name: string): ReplaySubject<MeasurandGroup> | undefined {
    let subject$: ReplaySubject<MeasurandGroup>;
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

  private setToLoading(subject$: ReplaySubject<MeasurandGroup>, groupName: string) {
    subject$.next({isLoading: true, name: groupName, values: []});
  }

  private getSelectableMeasurands(url: string, params: any, groupName: string): Observable<MeasurandGroup> {
    return this.http.get<SelectableMeasurand[]>(url, {params}).pipe(
      handleError(),
      map(dtos => ({
        isLoading: false,
        name: "frontend.de.iteratec.isr.measurand.group." + groupName, values: dtos
      })),
    )
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
