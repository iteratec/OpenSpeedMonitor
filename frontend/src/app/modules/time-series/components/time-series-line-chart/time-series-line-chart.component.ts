import {
  AfterContentInit,
  Component,
  ElementRef,
  HostListener,
  Input,
  OnChanges,
  SimpleChanges,
  ViewChild,
  ViewEncapsulation
} from "@angular/core";

import {EventResultData} from '../../models/event-result-data.model';
import {LineChartService} from '../../services/line-chart.service';


@Component({
  selector: 'osm-time-series-line-chart',
  encapsulation: ViewEncapsulation.None,  // needed! otherwise the scss style do not apply to svg content (see https://stackoverflow.com/questions/36214546/styles-in-component-for-d3-js-do-not-show-in-angular-2/36214723#36214723)
  templateUrl: './time-series-line-chart.component.html',
  styleUrls: [ './time-series-line-chart.component.scss' ]
})
export class TimeSeriesLineChartComponent implements AfterContentInit, OnChanges {

  @Input()
  timeSeriesResults: EventResultData;

  @ViewChild("svg")
  svgElement: ElementRef;

  private _resizeTimeoutId: number;

  constructor(
    private lineChartService: LineChartService
  ) {}

  @HostListener('window:resize', ['$event'])
  windowIsResized() {
    this.lineChartService.startResize(this.svgElement);

    // Wati until the resize is done before redrawing the chart
    clearTimeout(this._resizeTimeoutId);
    this._resizeTimeoutId = window.setTimeout(() => {
      this.lineChartService.resizeChart(this.svgElement);
      this.redraw();

      this.lineChartService.endResize(this.svgElement);
    }, 500);
  }

  redraw() {
    this.lineChartService.drawLineChart(this.timeSeriesResults);
    this.lineChartService.drawLegends(this.timeSeriesResults)
  }

  ngAfterContentInit(): void {
    this.lineChartService.initChart(this.svgElement);
    this.lineChartService.initLegendData(this.timeSeriesResults)
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.redraw();
  }

}
