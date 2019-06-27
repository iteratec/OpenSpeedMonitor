import {
  Caller,
  ResultSelectionCommand,
  ResultSelectionCommandParameter
} from "../models/result-selection-command.model";
import {BehaviorSubject} from "rxjs";
import {MeasuredEvent} from "../../../models/measured-event.model";
import {Location} from "../../../models/location.model";
import {Connectivity} from "../../../models/connectivity.model";
import {Injectable} from "@angular/core";
import {URL} from "../../../enums/url.enum";
import {ApplicationWithPages, SelectableApplication} from "../../../models/application.model";
import {ResponseWithLoadingState} from "../../../models/response-with-loading-state.model";
import {MeasurandGroup, SelectableMeasurand} from "../../../models/measurand.model";
import {ResultSelectionService} from "./result-selection.service";
import {UiComponent} from "../../../enums/ui-component.enum";
import {RemainingGetBarchartCommand} from "../../chart/models/get-barchart-command.model";

@Injectable()
export class ResultSelectionStore {
  from: Date;
  to: Date;
  _resultSelectionCommand$: BehaviorSubject<ResultSelectionCommand>;
  _remainingGetBarchartCommand$: BehaviorSubject<RemainingGetBarchartCommand>;

  applications$: BehaviorSubject<ResponseWithLoadingState<SelectableApplication[]>> = new BehaviorSubject({isLoading: false, data: []});
  applicationsAndPages$: BehaviorSubject<ResponseWithLoadingState<ApplicationWithPages[]>> = new BehaviorSubject({isLoading: false, data: []});
  eventsAndPages$: BehaviorSubject<ResponseWithLoadingState<MeasuredEvent[]>> = new BehaviorSubject({isLoading: false, data: []});
  locationsAndBrowsers$: BehaviorSubject<ResponseWithLoadingState<Location[]>> = new BehaviorSubject({isLoading: false, data: []});
  connectivities$: BehaviorSubject<ResponseWithLoadingState<Connectivity[]>> = new BehaviorSubject({isLoading: false, data: []});
  loadTimes$: BehaviorSubject<MeasurandGroup> = new BehaviorSubject({isLoading: false, name: "", values: []});
  userTimings$: BehaviorSubject<MeasurandGroup> = new BehaviorSubject({isLoading: false, name: "", values: []});
  heroTimings$: BehaviorSubject<MeasurandGroup> = new BehaviorSubject({isLoading: false, name: "", values: []});
  requestCounts$: BehaviorSubject<MeasurandGroup> = new BehaviorSubject({isLoading: false, name: "", values: []});
  requestSizes$: BehaviorSubject<MeasurandGroup> = new BehaviorSubject({isLoading: false, name: "", values: []});
  percentages$: BehaviorSubject<MeasurandGroup> = new BehaviorSubject({isLoading: false, name: "", values: []});
  resultCount$: BehaviorSubject<number> = new BehaviorSubject<number>(0);

  constructor(private resultSelectionService: ResultSelectionService) {
    let defaultFrom = new Date();
    let defaultTo = new Date();
    defaultFrom.setDate(defaultTo.getDate() - 3);
    this._resultSelectionCommand$ = new BehaviorSubject({from: defaultFrom, to: defaultTo, caller: Caller.EventResult});
    this._remainingGetBarchartCommand$ = new BehaviorSubject({});
  }

  registerComponent(component: UiComponent): void {
    if (component === UiComponent.MEASURAND) {
      this.loadMeasurands(this.resultSelectionCommand);
    }

    this._resultSelectionCommand$.subscribe(state => {
      if (component === UiComponent.APPLICATION) {
        this.loadSelectableApplications(state);
      } else if (component === UiComponent.PAGE) {
        this.loadSelectableEventsAndPages(state);
      } else if (component === UiComponent.LOCATION) {
        this.loadSelectableLocationsAndBrowsers(state);
      } else if (component === UiComponent.CONNECTIVITY) {
        this.loadSelectableConnectivities(state);
      } else if (component === UiComponent.MEASURAND) {
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

  setRemainingGetBarchartCommandComparativeTimeFrame(timeFrame: Date[]): void {
    this.setRemainingGetBarchartCommand({...this.remainingGetBarchartCommand, fromComparative: timeFrame[0], toComparative: timeFrame[1]});
  }

  setRemainingGetBarchartCommandIds(ids: number[], type: ResultSelectionCommandParameter): void {
    this.setRemainingGetBarchartCommand({...this.remainingGetBarchartCommand, [type]: ids});
  }

  setMeasurands(measurands: string[]): void {
    this.setRemainingGetBarchartCommand({...this.remainingGetBarchartCommand, measurands: measurands});
  }

  get remainingGetBarchartCommand(): RemainingGetBarchartCommand {
    return this._remainingGetBarchartCommand$.getValue();
  }

  setResultSelectionCommand(newState: ResultSelectionCommand): void {
    this._resultSelectionCommand$.next(newState);
    this.loadResultCount(newState);
  }

  private setRemainingGetBarchartCommand(newState: RemainingGetBarchartCommand): void {
    this._remainingGetBarchartCommand$.next(newState);
  }

  private loadSelectableApplications(resultSelectionCommand: ResultSelectionCommand): void {
    this.applications$.next({...this.applications$.getValue(), isLoading: true});
    this.resultSelectionService.fetchResultSelectionData<SelectableApplication[]>(resultSelectionCommand, URL.APPLICATIONS)
      .subscribe(next => this.applications$.next({isLoading: false, data: next}));
  }

  private loadSelectableApplicationsAndPages(resultSelectionCommand: ResultSelectionCommand): void {
    this.applicationsAndPages$.next({...this.applicationsAndPages$.getValue(), isLoading: true});
    this.resultSelectionService.fetchResultSelectionData<ApplicationWithPages[]>(resultSelectionCommand, URL.APPLICATIONS_AND_PAGES)
      .subscribe(next => this.applicationsAndPages$.next({isLoading: false, data: next}));
  }

  private loadSelectableEventsAndPages(resultSelectionCommand: ResultSelectionCommand): void {
    this.eventsAndPages$.next({...this.eventsAndPages$.getValue(), isLoading: true});
    this.resultSelectionService.fetchResultSelectionData<MeasuredEvent[]>(resultSelectionCommand, URL.EVENTS_AND_PAGES)
      .subscribe(next => this.eventsAndPages$.next({isLoading: false, data: next}));
  }

  private loadSelectableLocationsAndBrowsers(resultSelectionCommand: ResultSelectionCommand): void {
    this.locationsAndBrowsers$.next({...this.locationsAndBrowsers$.getValue(), isLoading: true});
    this.resultSelectionService.fetchResultSelectionData<Location[]>(resultSelectionCommand, URL.LOCATIONS_AND_BROWSERS)
      .subscribe(next => this.locationsAndBrowsers$.next({isLoading: false, data: next}));
  }

  private loadSelectableConnectivities(resultSelectionCommand: ResultSelectionCommand): void {
    this.connectivities$.next({...this.connectivities$.getValue(), isLoading: true});
    this.resultSelectionService.fetchResultSelectionData<Connectivity[]>(resultSelectionCommand, URL.CONNECTIVITIES)
      .subscribe(next => this.connectivities$.next({isLoading: false, data: next}));
  }

  private loadMeasurands(resultSelectionCommand: ResultSelectionCommand): void {
    this.loadTimes$.next({...this.loadTimes$.getValue(), isLoading: true});
    this.requestCounts$.next({...this.requestCounts$.getValue(), isLoading: true});
    this.requestSizes$.next({...this.requestSizes$.getValue(), isLoading: true});
    this.percentages$.next({...this.percentages$.getValue(), isLoading: true});
    this.resultSelectionService.fetchResultSelectionData<MeasurandGroup[]>(resultSelectionCommand, URL.MEASURANDS)
      .subscribe((groups: MeasurandGroup[]) => {
      if (groups) {
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
      }
    });
  }

  private loadUserTimings(resultSelectionCommand: ResultSelectionCommand): void {
    this.userTimings$.next({...this.userTimings$.getValue(), isLoading: true});
    this.resultSelectionService.fetchResultSelectionData<SelectableMeasurand[]>(resultSelectionCommand, URL.USER_TIMINGS)
      .subscribe(next => {
      const groupName: string = "User Timings";
      let responseWithLoadingState: MeasurandGroup = {
        isLoading: false,
        name: groupName,
        values: next
      };
      this.userTimings$.next(responseWithLoadingState);
    });
  }

  private loadHeroTimings(resultSelectionCommand: ResultSelectionCommand): void {
    this.heroTimings$.next({...this.heroTimings$.getValue(), isLoading: true});
    this.resultSelectionService.fetchResultSelectionData<SelectableMeasurand[]>(resultSelectionCommand, URL.HERO_TIMINGS)
      .subscribe(next => {
      const groupName: string = "Hero Timings";
      let responseWithLoadingState: MeasurandGroup = {
        isLoading: false,
        name: groupName,
        values: next
      };
      this.heroTimings$.next(responseWithLoadingState);
    });
  }

  private loadResultCount(resultSelectionCommand: ResultSelectionCommand): void {
    this.resultSelectionService.fetchResultSelectionData<string>(resultSelectionCommand, URL.RESULT_COUNT)
      .subscribe(next => this.resultCount$.next(+next));
  }

  private getDefaultSubjectByMeasurandGroup(name: string): BehaviorSubject<MeasurandGroup> | undefined {
    let subject$: BehaviorSubject<MeasurandGroup>;
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
