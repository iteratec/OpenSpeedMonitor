import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EMPTY, Observable, OperatorFunction, ReplaySubject, combineLatest} from "rxjs";
import {MeasurandGroup, SelectableMeasurand} from "../models/measurand.model";
import {ResponseWithLoadingState} from "../models/response-with-loading-state.model";
import {catchError, switchMap, map} from "rxjs/operators";
import {Application} from "../models/application.model";
import {Page} from "../models/page.model";
import {group} from "@angular/animations";

@Injectable({
  providedIn: 'root'
})
export class ResultSelectionService {
  loadTimes$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  userTimings$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  heroTimings$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  requestCounts$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  requestSizes$:  ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  percentages$:  ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);

  selectedApplications$: ReplaySubject<Application[]> = new ReplaySubject<Application[]>(1);
  selectedPages$: ReplaySubject<Page[]> = new ReplaySubject<Page[]>(1);

  constructor(private http: HttpClient) {
    this.getMeasurands();

    this.combinedParams().pipe(
      switchMap(params => this.getUserTimings(params))
    ).subscribe(this.userTimings$);

    this.combinedParams().pipe(
      switchMap(params => this.getHeroTimings(params))
    ).subscribe(this.heroTimings$)
  }

  updateApplications(applications: Application[]){
    this.selectedApplications$.next(applications);
    this.updatePages([]);
  }
  updatePages(pages: Page[]){
    this.selectedPages$.next(pages);
  }

  private combinedParams():Observable<any>{
    return combineLatest(
      this.selectedApplications$,
      this.selectedPages$,
      (applications: Application[], pages:Page[]) => this.generateParams(applications, pages));
  }
  private generateParams(applications: Application[], pages: Page[]){
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

  private getUserTimings(params): Observable<ResponseWithLoadingState<MeasurandGroup>>{
    const userTimingsUrl: string = '/resultSelection/getUserTimings';
    const groupName: string =  "USER_TIMINGS";
    this.setToLoading(this.userTimings$, groupName);
    return this.getSelectableMeasurands(userTimingsUrl, params, groupName);
  }
  private getHeroTimings(params): Observable<ResponseWithLoadingState<MeasurandGroup>>{
    const heroTimingsUrl: string = '/resultSelection/getHeroTimings';
    const groupName: string =  "HERO_TIMINGS";
    this.setToLoading(this.heroTimings$, groupName);
    return this.getSelectableMeasurands(heroTimingsUrl, params, groupName);
  }

  private getMeasurands(){
    this.setToLoading( this.loadTimes$, "LOAD_TIMES");
    this.setToLoading(this.requestCounts$,"REQUEST_COUNTS");
    this.setToLoading( this.requestSizes$, "REQUEST_SIZES");
    this.setToLoading( this.percentages$, "PERCENTAGES");
    const url: string = '/resultSelection/getMeasurands';
    return this.http.get<MeasurandGroup[]>(url).pipe(
      handleError()
    ).subscribe((groups: MeasurandGroup[]) => {
      groups.forEach((group: MeasurandGroup)=>{
        let responseWithLoadingState: ResponseWithLoadingState<MeasurandGroup> = {isLoading: false, data: {name: "frontend.de.iteratec.isr.measurand.group."+group.name, values: group.values.map(measurand => ({name: "frontend.de.iteratec.isr.measurand."+measurand.name, id: measurand.id }))}};
        let concerningSubject$ = this.getDefaultSubjectByMeasurandGroup(group.name);
        if(concerningSubject$){
          concerningSubject$.next(responseWithLoadingState);
        }
      });
    })
  }

  private getDefaultSubjectByMeasurandGroup(name: string): ReplaySubject<ResponseWithLoadingState<MeasurandGroup>>|undefined{
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

  private setToLoading(subject$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>>, groupName: string){
    subject$.next({isLoading: true, data: {name: groupName, values:[]}});
  }

  private getSelectableMeasurands(url: string, params: any,groupName: string): Observable<ResponseWithLoadingState<MeasurandGroup>>{
    return this.http.get<SelectableMeasurand[]>(url, {params}).pipe(
      handleError(),
      map(dtos => ({isLoading: false, data: {name: "frontend.de.iteratec.isr.measurand.group."+groupName, values: dtos}})),
    )
  }
}

function handleError(): OperatorFunction<any, any> {
  return catchError((error) => {
    console.log(error);
    return EMPTY;
  });
}
