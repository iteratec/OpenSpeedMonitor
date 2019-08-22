import {
  AfterContentInit,
  Component, ElementRef,
  OnChanges,
  SimpleChanges, ViewChild,
  Input,
  HostListener
} from "@angular/core/";

import {TimeSeriesResults} from '../../models/time-series-results.model';
import {LineChartService} from '../../services/line-chart.service';


@Component({
  selector: 'osm-time-series-line-chart',
  templateUrl: './time-series-line-chart.component.html',
  styleUrls: [ './time-series-line-chart.component.scss' ]
})
export class TimeSeriesLineChartComponent implements AfterContentInit, OnChanges {

  @Input()
  timeSeriesResults: TimeSeriesResults;

  @ViewChild("svg") 
  svgElement: ElementRef;


  constructor(
    private lineChartService: LineChartService
  ) {}

  @HostListener('window:resize', ['$event'])
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
