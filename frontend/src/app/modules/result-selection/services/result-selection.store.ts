import {
  Caller,
  ResultSelectionCommand,
  ResultSelectionCommandParameter
} from "../models/result-selection-command.model";
import {BehaviorSubject, ReplaySubject} from "rxjs";
import {MeasuredEvent} from "../../../models/measured-event.model";
import {Location} from "../../../models/location.model";
import {Connectivity} from "../../../models/connectivity.model";
import {Injectable} from "@angular/core";
import {ApplicationWithPages, SelectableApplication} from "../../../models/application.model";
import {ResponseWithLoadingState} from "../../../models/response-with-loading-state.model";
import {MeasurandGroup, SelectableMeasurand} from "../../../models/measurand.model";
import {ResultSelectionService, URL} from "./result-selection.service";

export enum UiComponent {
  APPLICATION, PAGE_LOCATION_CONNECTIVITY, MEASURAND
}

@Injectable()
export class ResultSelectionStore {
  from: Date;
  to: Date;
  _resultSelectionCommand$: BehaviorSubject<ResultSelectionCommand>;

  applications$: BehaviorSubject<SelectableApplication[]> = new BehaviorSubject([]);
  applicationsAndPages$: BehaviorSubject<ApplicationWithPages[]> = new BehaviorSubject([]);
  eventsAndPages$: BehaviorSubject<MeasuredEvent[]> = new BehaviorSubject([]);
  locationsAndBrowsers$: BehaviorSubject<Location[]> = new BehaviorSubject([]);
  connectivities$: BehaviorSubject<Connectivity[]> = new BehaviorSubject([]);
  loadTimes$: BehaviorSubject<ResponseWithLoadingState<MeasurandGroup>> = new BehaviorSubject({isLoading: false, data: {name: "", values: []}});
  userTimings$: BehaviorSubject<ResponseWithLoadingState<MeasurandGroup>> = new BehaviorSubject({isLoading: false, data: {name: "", values: []}});
  heroTimings$: BehaviorSubject<ResponseWithLoadingState<MeasurandGroup>> = new BehaviorSubject({isLoading: false, data: {name: "", values: []}});
  requestCounts$: BehaviorSubject<ResponseWithLoadingState<MeasurandGroup>> = new BehaviorSubject({isLoading: false, data: {name: "", values: []}});
  requestSizes$: BehaviorSubject<ResponseWithLoadingState<MeasurandGroup>> = new BehaviorSubject({isLoading: false, data: {name: "", values: []}});
  percentages$: BehaviorSubject<ResponseWithLoadingState<MeasurandGroup>> = new BehaviorSubject({isLoading: false, data: {name: "", values: []}});
  resultCount$: BehaviorSubject<number> = new BehaviorSubject<number>(-1);

  constructor(private resultSelectionService: ResultSelectionService) {
    let defaultFrom = new Date();
    let defaultTo = new Date();
    defaultFrom.setDate(defaultTo.getDate() - 3);
    this._resultSelectionCommand$ = new BehaviorSubject({from:defaultFrom, to: defaultTo, caller: Caller.EventResult});
  }

  registerComponent(component: UiComponent): void {
    this._resultSelectionCommand$.subscribe(state => {
      if(component === UiComponent.APPLICATION) {
        this.loadSelectableApplications(state);
      } else if(component === UiComponent.PAGE_LOCATION_CONNECTIVITY) {
        this.loadSelectableEventsAndPages(state);
        this.loadSelectableLocationsAndBrowsers(state);
        this.loadSelectableConnectivities(state);
      } else if(component === UiComponent.MEASURAND) {
        this.loadMeasurands(state);
        this.loadUserTimings(state);
        this.loadHeroTimings(state);
      }
    });
  }

  setResultSelectionCommandTimeFrame(timeFrame: Date[]): void {
    this.setResultSelectionCommand({...this.resultSelectionCommand, from: timeFrame[0], to: timeFrame[1]});
  }

  setResultSelectionCommandIds(ids: number[], type: ResultSelectionCommandParameter): void {
    this.setResultSelectionCommand({...this.resultSelectionCommand, [type]: ids});
  }

  get resultSelectionCommand(): ResultSelectionCommand {
    return this._resultSelectionCommand$.getValue();
  }

  private setResultSelectionCommand(newState: ResultSelectionCommand): void {
    this.loadResultCount(newState);
    this._resultSelectionCommand$.next(newState);
  }

  private loadSelectableApplications(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.fetchResultSelectionData<SelectableApplication[]>(resultSelectionCommand, URL.APPLICATIONS)
      .subscribe(next => this.applications$.next(next));
  }

  private loadSelectableApplicationsAndPages(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.fetchResultSelectionData<ApplicationWithPages[]>(resultSelectionCommand, URL.APPLICATIONS_AND_PAGES)
      .subscribe(next => this.applicationsAndPages$.next(next));
  }

  private loadSelectableEventsAndPages(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.fetchResultSelectionData<MeasuredEvent[]>(resultSelectionCommand, URL.EVENTS_AND_PAGES)
      .subscribe(next => this.eventsAndPages$.next(next));
  }

  private loadSelectableLocationsAndBrowsers(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.fetchResultSelectionData<Location[]>(resultSelectionCommand, URL.LOCATIONS_AND_BROWSERS)
      .subscribe(next => this.locationsAndBrowsers$.next(next));
  }

  private loadSelectableConnectivities(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.fetchResultSelectionData<Connectivity[]>(resultSelectionCommand, URL.CONNECTIVITIES)
      .subscribe(next => this.connectivities$.next(next));
  }

  private loadMeasurands(resultSelectionCommand: ResultSelectionCommand): void {
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

  private loadUserTimings(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.fetchResultSelectionData<SelectableMeasurand[]>(resultSelectionCommand, URL.USER_TIMINGS)
      .subscribe(next => {
      const groupName: string = "User Timings";
      let responseWithLoadingState: ResponseWithLoadingState<MeasurandGroup> = {
        isLoading: false,
        data: {name: groupName, values: next}
      };
      this.userTimings$.next(responseWithLoadingState);
    });
  }

  private loadHeroTimings(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.fetchResultSelectionData<SelectableMeasurand[]>(resultSelectionCommand, URL.HERO_TIMINGS)
      .subscribe(next => {
      const groupName: string = "Hero Timings";
      let responseWithLoadingState: ResponseWithLoadingState<MeasurandGroup> = {
        isLoading: false,
        data: {name: groupName, values: next}
      };
      this.heroTimings$.next(responseWithLoadingState);
    });
  }

  private loadResultCount(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.fetchResultSelectionData<string>(resultSelectionCommand, URL.RESULT_COUNT)
      .subscribe(next => this.resultCount$.next(+next));
  }

  private getDefaultSubjectByMeasurandGroup(name: string): BehaviorSubject<ResponseWithLoadingState<MeasurandGroup>> | undefined {
    let subject$: BehaviorSubject<ResponseWithLoadingState<MeasurandGroup>>;
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
