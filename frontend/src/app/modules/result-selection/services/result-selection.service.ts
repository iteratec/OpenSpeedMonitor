import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EMPTY, Observable, OperatorFunction, ReplaySubject} from "rxjs";
import {SelectableApplication} from "../models/selectable-application.model";
import {Caller, ResultSelectionCommand} from "../models/result-selection-command.model";
import {catchError, startWith} from "rxjs/operators";
import {SelectableMeasuredEvent} from "../models/selectable-measured-event.model";
import {SelectableLocation} from "../models/selectable-location.model";
import {SelectableConnectivity} from "../models/selectable-connectivity.model";
import {SelectableHeroTiming} from "../models/selectable-hero-timing.model";
import {SelectableUserTiming} from "../models/selectable-user-timing.model";

@Injectable()
export class ResultSelectionService {
  selectableApplications$: ReplaySubject<SelectableApplication[]> = new ReplaySubject<SelectableApplication[]>(1);
  selectableEventsAndPages$: ReplaySubject<SelectableMeasuredEvent[]> = new ReplaySubject<SelectableMeasuredEvent[]>(1);
  selectableLocationsAndBrowsers$: ReplaySubject<SelectableLocation[]> = new ReplaySubject<SelectableLocation[]>(1);
  selectableConnectivities$: ReplaySubject<SelectableConnectivity[]> = new ReplaySubject<SelectableConnectivity[]>(1);
  selectableHeroTimings$: ReplaySubject<SelectableHeroTiming[]> = new ReplaySubject<SelectableHeroTiming[]>(1);
  selectableUserTimings$: ReplaySubject<SelectableUserTiming[]> = new ReplaySubject<SelectableUserTiming[]>(1);
  resultCount$: ReplaySubject<string> = new ReplaySubject<string>(1);


  constructor(private http: HttpClient) {
  }

  loadAllSelectableData(resultSelectionCommand: ResultSelectionCommand): void {
    this.loadSelectableApplications(resultSelectionCommand);
    this.loadSelectableEventsAndPages(resultSelectionCommand);
    this.loadSelectableLocationsAndBrowsers(resultSelectionCommand);
    this.loadSelectableConnectivities(resultSelectionCommand);
    this.loadSelectableUserTimings(resultSelectionCommand);
    this.loadSelectableHeroTimings(resultSelectionCommand);
    this.loadResultCount(resultSelectionCommand);
  }

  loadSelectableApplications(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableApplications(resultSelectionCommand).subscribe(next => this.selectableApplications$.next(next));
  }

  loadSelectableEventsAndPages(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableEventsAndPages(resultSelectionCommand).subscribe(next => this.selectableEventsAndPages$.next(next));
  }

  loadSelectableLocationsAndBrowsers(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableLocationsAndBrowsers(resultSelectionCommand).subscribe(next => this.selectableLocationsAndBrowsers$.next(next));
  }

  loadSelectableConnectivities(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableConnectivities(resultSelectionCommand).subscribe(next => this.selectableConnectivities$.next(next));
  }

  loadSelectableUserTimings(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableUserTimings(resultSelectionCommand).subscribe(next => this.selectableUserTimings$.next(next));
  }

  loadSelectableHeroTimings(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateSelectableHeroTimings(resultSelectionCommand).subscribe(next => this.selectableHeroTimings$.next(next));
  }

  loadResultCount(resultSelectionCommand: ResultSelectionCommand): void {
    this.updateResultCount(resultSelectionCommand).subscribe(next => this.resultCount$.next(next));
  }

  updateSelectableApplications(resultSelectionCommand: ResultSelectionCommand): Observable<SelectableApplication[]> {
    const params = this.createParams(resultSelectionCommand);
    return this.http.get<SelectableApplication[]>('/resultSelection/getJobGroups', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  updateSelectableEventsAndPages(resultSelectionCommand: ResultSelectionCommand): Observable<SelectableMeasuredEvent[]> {
    const params = this.createParams(resultSelectionCommand);
    return this.http.get<SelectableMeasuredEvent[]>('/resultSelection/getMeasuredEvents', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  updateSelectableLocationsAndBrowsers(resultSelectionCommand: ResultSelectionCommand): Observable<SelectableLocation[]> {
    const params = this.createParams(resultSelectionCommand);
    return this.http.get<SelectableLocation[]>('/resultSelection/getLocations', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  updateSelectableConnectivities(resultSelectionCommand: ResultSelectionCommand): Observable<SelectableConnectivity[]> {
    const params = this.createParams(resultSelectionCommand);
    return this.http.get('/resultSelection/getConnectivityProfiles', {params: params}).pipe(
      handleError(),
      startWith(null)
    )
  }

  updateSelectableUserTimings(resultSelectionCommand: ResultSelectionCommand): Observable<SelectableUserTiming[]> {
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
      jobGroupIds: resultSelectionCommand.jobGroupIds ? resultSelectionCommand.jobGroupIds.toString() : undefined,
      pageIds: resultSelectionCommand.pageIds ? resultSelectionCommand.pageIds.toString() : undefined,
      measuredEventIds: resultSelectionCommand.measuredEventIds ? resultSelectionCommand.measuredEventIds.toString() : undefined,
      browserIds: resultSelectionCommand.browserIds ? resultSelectionCommand.browserIds.toString() : undefined,
      locationIds: resultSelectionCommand.locationIds ? resultSelectionCommand.locationIds.toString(): undefined,
      selectedConnectivities: resultSelectionCommand.selectedConnectivities ? resultSelectionCommand.selectedConnectivities.toString(): undefined
    }
  }
}


function handleError(): OperatorFunction<any, any> {
  return catchError((error) => {
    console.log(error);
    return EMPTY;
  });
}
