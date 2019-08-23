import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {BarchartDataService} from "./services/barchart-data.service";
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {AggregationChartDataService} from "./services/aggregation-chart-data.service";
import {BehaviorSubject} from "rxjs";
import {SpinnerService} from "../shared/services/spinner.service";

@Component({
  selector: 'osm-aggregation',
  templateUrl: './aggregation.component.html',
  styleUrls: ['./aggregation.component.scss']
})
export class AggregationComponent implements OnInit {


  isHidden: boolean;
  barchartAverageData$: BehaviorSubject<any> = new BehaviorSubject<any>([]);
  barchartMedianData$: BehaviorSubject<any> = new BehaviorSubject<any>([]);

  constructor(private barchartDataService: BarchartDataService, private resultSelectionStore: ResultSelectionStore, private aggregationChartDataService: AggregationChartDataService, private spinnerService: SpinnerService) {
    this.aggregationChartDataService.barchartAverageData$.subscribe((data) =>{
      this.barchartAverageData$.next(data);
    });
    this.aggregationChartDataService.barchartMedianData$.subscribe((data) => {
      this.barchartMedianData$.next(data);
    })
  }

  ngOnInit() {
    this.isHidden = true;
    this.aggregationChartDataService.isLoading$.next(false);
  }

  getBarchartData(): void {
    this.isHidden = false;
    console.log("getBarchartData");
    this.aggregationChartDataService.isLoading$.next(true);
    this.aggregationChartDataService.getBarchartData(this.resultSelectionStore.resultSelectionCommand,this.resultSelectionStore.remainingResultSelection);
    /*this.isLoading = this.aggregationChartDataService.isLoading$.getValue();
    console.log("getBarchartData this.isLoading: " + this.isLoading);*/
  }
}
