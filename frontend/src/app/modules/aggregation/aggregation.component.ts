import {Component, OnInit} from '@angular/core';
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {URL} from "../../enums/url.enum";
import {BarchartDataService} from "../chart/services/barchart-data.service";
import {BehaviorSubject, Observable, Subject} from "rxjs";
import {ResultSelectionCommand} from "../result-selection/models/result-selection-command.model";
import {RemainingGetBarchartCommand} from "../chart/models/get-barchart-command.model";

@Component({
  selector: 'osm-aggregation',
  templateUrl: './aggregation.component.html',
  styleUrls: ['./aggregation.component.scss']
})
export class AggregationComponent implements OnInit {

  areApplicationsSelected$: BehaviorSubject<boolean> = new BehaviorSubject(false);
  arePagesSelected$: BehaviorSubject<boolean> = new BehaviorSubject(false);
  areMeasuredEventsSelected$: BehaviorSubject<boolean> = new BehaviorSubject(false);
  areMeasurandsSelected$: BehaviorSubject<boolean> = new BehaviorSubject(false);
  resultCount$: Observable<number>;

  resetResultSelectionEvent: Subject<void> = new Subject<void>();

  constructor(private barchartDataService: BarchartDataService, private resultSelectionStore: ResultSelectionStore) {
    this.resultCount$ = this.resultSelectionStore.resultCount$;
  }

  ngOnInit() {
    this.resultSelectionStore._resultSelectionCommand$.subscribe((next: ResultSelectionCommand) => {
      if (next.jobGroupIds) {
        this.areApplicationsSelected$.next(next.jobGroupIds.length > 0);
      }
      if (next.pageIds) {
        this.arePagesSelected$.next(next.pageIds.length > 0);
      }
      if (next.measuredEventIds) {
        this.areMeasuredEventsSelected$.next(next.measuredEventIds.length > 0);
      }
    });
    this.resultSelectionStore._remainingGetBarchartCommand$.subscribe((next: RemainingGetBarchartCommand) => {
      if (next.measurands) {
        this.areMeasurandsSelected$.next(next.measurands.length > 0);
      }
    });
  }

  getBarchartData(): void {
    this.barchartDataService.fetchBarchartData<any>(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingGetBarchartCommand,
      "avg",
      URL.AGGREGATION_BARCHART_DATA
    ).subscribe(result => console.log(result));

    this.barchartDataService.fetchBarchartData<any>(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingGetBarchartCommand,
      50,
      URL.AGGREGATION_BARCHART_DATA
    ).subscribe(result => console.log(result));
  }

  emitResetEventToComponent() {
    this.resetResultSelectionEvent.next();
  }
}
