import {Component, OnInit} from '@angular/core';
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {URL} from "../../enums/url.enum";
import {BarchartDataService} from "../chart/services/barchart-data.service";

@Component({
  selector: 'osm-aggregation',
  templateUrl: './aggregation.component.html',
  styleUrls: ['./aggregation.component.scss']
})
export class AggregationComponent implements OnInit {

  constructor(private barchartDataService: BarchartDataService, private resultSelectionStore: ResultSelectionStore) { }

  ngOnInit() {
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

  isGetBarchartDataAllowed(): boolean {
    return this.resultSelectionStore.resultSelectionCommand.from
      && this.resultSelectionStore.resultSelectionCommand.to
      && this.areApplicationsSelected()
      && this.areMeasurandsSelected()
  }

  isDataAvailable(): boolean {
    return this.resultSelectionStore.resultCount$.getValue() > 0;
  }

  areApplicationsSelected(): boolean {
    return this.resultSelectionStore.resultSelectionCommand.jobGroupIds &&
      this.resultSelectionStore.resultSelectionCommand.jobGroupIds.length > 0;
  }

  arePagesSelected() {
    return true;
  }

  areMeasurandsSelected() {
    return true;
  }

  isProcessTimeLong(): boolean {
    return this.resultSelectionStore.resultCount$.getValue() == -1 &&
      ((this.resultSelectionStore.resultSelectionCommand.pageIds && this.resultSelectionStore.resultSelectionCommand.pageIds.length > 0
          || this.resultSelectionStore.resultSelectionCommand.measuredEventIds && this.resultSelectionStore.resultSelectionCommand.measuredEventIds.length > 0 )
        && this.resultSelectionStore.resultSelectionCommand.jobGroupIds && this.resultSelectionStore.resultSelectionCommand.jobGroupIds.length > 0
      );
  }
}
