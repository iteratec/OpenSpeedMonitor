import {Caller, ResultSelectionCommand} from "../models/result-selection-command.model";
import {BehaviorSubject, ReplaySubject} from "rxjs";
import {ResultSelectionService} from "./result-selection.service";
import {MeasuredEvent} from "../../../models/measured-event.model";
import {Location} from "../../../models/location.model";
import {Connectivity} from "../../../models/connectivity.model";
import {Injectable} from "@angular/core";
import {ApplicationWithPages, SelectableApplication} from "../../../models/application.model";

@Injectable()
export class ResultSelectionStore {
  from: Date;
  to: Date;
  _resultSelectionCommand$: BehaviorSubject<ResultSelectionCommand>;


  applications$: ReplaySubject<SelectableApplication[]> = new ReplaySubject(1);
  applicationsAndPages$: ReplaySubject<ApplicationWithPages[]> = new ReplaySubject(1);
  eventsAndPages$: BehaviorSubject<MeasuredEvent[]> = new BehaviorSubject([]);
  locationsAndBrowsers$: BehaviorSubject<Location[]> = new BehaviorSubject([]);
  connectivities$: ReplaySubject<Connectivity[]> = new ReplaySubject(1);


  constructor(private resultSelectionService: ResultSelectionService){
    let defaultFrom = new Date();
    let defaultTo = new Date();
    defaultFrom.setDate(defaultTo.getDate() - 3);
    this._resultSelectionCommand$ = new BehaviorSubject({from:defaultFrom, to: defaultTo, caller: Caller.EventResult});
  }

  setSelectedTimeFrame(selectedTimeFrame: Date[]) {
    this.setResultSelectionCommand({...this.resultSelectionCommand, from: selectedTimeFrame[0], to:selectedTimeFrame[1]});
  }

  setSelectedJobGroups(ids: number[]){
    this.setResultSelectionCommand({...this.resultSelectionCommand, jobGroupIds: ids});
  }

  setSelectedPages(ids: number[]){
    this.setResultSelectionCommand({...this.resultSelectionCommand, pageIds: ids});
  }

  setSelectedBrowser(ids: number[]){
    this.setResultSelectionCommand({...this.resultSelectionCommand, browserIds: ids});
  }

  setSelectedConnectivities(connectivities: string[]){
    this.setResultSelectionCommand({...this.resultSelectionCommand, selectedConnectivities: connectivities})
  }

  setSelectedLocations(ids: number[]){
    this.setResultSelectionCommand({...this.resultSelectionCommand, locationIds: ids});
  }

  get resultSelectionCommand(){
    return this._resultSelectionCommand$.getValue();
  }

  setResultSelectionCommand(newState: ResultSelectionCommand){
    this._resultSelectionCommand$.next(newState);
  }

  loadSelectableApplications(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.updateSelectableApplications(resultSelectionCommand).subscribe(next => this.applications$.next(next));
  }

  loadSelectableApplicationsAndPages(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.updateSelectableApplicationsAndPages(resultSelectionCommand).subscribe(next => this.applicationsAndPages$.next(next));
  }

  loadSelectableEventsAndPages(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.updateSelectableEventsAndPages(resultSelectionCommand).subscribe(next => this.eventsAndPages$.next(next));
  }

  loadSelectableLocationsAndBrowsers(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.updateSelectableLocationsAndBrowsers(resultSelectionCommand).subscribe(next => this.locationsAndBrowsers$.next(next));
  }

  loadSelectableConnectivities(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.updateSelectableConnectivities(resultSelectionCommand).subscribe(next => this.connectivities$.next(next));
  }

}
