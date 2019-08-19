import {
  AfterContentInit,
  Component, ElementRef,
  OnChanges,
  SimpleChanges, ViewChild,
  ViewEncapsulation,
  Input
} from "@angular/core/";

import {TimeSeriesResults} from '../../models/time-series-results.model';
import {LineChartService} from '../../services/line-chart.service';


@Component({
  selector: 'osm-time-series-line-chart',
  template: '<div class="time-series-line-chart-container"><svg #svg ><g class="time-series-chart"></g></svg></div>',
  styleUrls: [ './time-series-line-chart.component.scss' ]
})
export class TimeSeriesLineChartComponent implements AfterContentInit, OnChanges {

  @Input()
  timeSeriesResults: TimeSeriesResults;

  @ViewChild("svg") 
  svgElement: ElementRef;  // TODO WHY IS THIS ELEMENT UNDEFINED????


  constructor(
    private lineChartService: LineChartService
  ) {}

  redraw() {
    this.lineChartService.drawLineChart(this.svgElement, this.timeSeriesResults);
  }

  ngAfterContentInit(): void {
    this.redraw();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.redraw();
  }

}
