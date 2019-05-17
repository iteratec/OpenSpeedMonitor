import {Caller, ResultSelectionCommand} from "../models/result-selection-command.model";
import {BehaviorSubject, ReplaySubject} from "rxjs";
import {MeasuredEvent} from "../../../models/measured-event.model";
import {Location} from "../../../models/location.model";
import {Connectivity} from "../../../models/connectivity.model";
import {Injectable} from "@angular/core";
import {ApplicationWithPages, SelectableApplication} from "../../../models/application.model";
import {ResponseWithLoadingState} from "../../../models/response-with-loading-state.model";
import {MeasurandGroup} from "../../../models/measurand.model";
import {ResultSelectionService} from "./result-selection.service";

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
  loadTimes$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  userTimings$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  heroTimings$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  requestCounts$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  requestSizes$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  percentages$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>> = new ReplaySubject(1);
  resultCount$: ReplaySubject<string> = new ReplaySubject<string>(1);


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

  setSelectedConnectivities(connectivities: number[]){
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

  loadSelectableData(resultSelectionCommand: ResultSelectionCommand): void {
    this.loadSelectableEventsAndPages(resultSelectionCommand);
    this.loadSelectableConnectivities(resultSelectionCommand);
    this.loadSelectableLocationsAndBrowsers(resultSelectionCommand);

  }

  loadMeasurands(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.updateMeasurands(resultSelectionCommand).subscribe((groups: MeasurandGroup[]) => {
      if (groups) {
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
      }
    });
  }

  loadUserTimings(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.updateUserTimings(resultSelectionCommand).subscribe(next => {
      const groupName: string = "User Timings";
      let responseWithLoadingState: ResponseWithLoadingState<MeasurandGroup> = {
        isLoading: false,
        data: {name: groupName, values: next}
      };
      this.userTimings$.next(responseWithLoadingState);
    });
  }

  loadHeroTimings(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.updateHeroTimings(resultSelectionCommand).subscribe(next => {
      const groupName: string = "Hero Timings";
      let responseWithLoadingState: ResponseWithLoadingState<MeasurandGroup> = {
        isLoading: false,
        data: {name: groupName, values: next}
      };
      this.heroTimings$.next(responseWithLoadingState);
    });
  }

  loadResultCount(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.updateResultCount(resultSelectionCommand).subscribe(next => this.resultCount$.next(next));
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
}
