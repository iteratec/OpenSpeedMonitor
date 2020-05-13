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
} from '@angular/core';

import {EventResultData} from '../../models/event-result-data.model';
import {LineChartService} from '../../services/line-chart.service';
import {NgxSmartModalService} from 'ngx-smart-modal';
import {SpinnerService} from '../../../shared/services/spinner.service';
import {TranslateService} from '@ngx-translate/core';


@Component({
  selector: 'osm-time-series-line-chart',
  // needed! otherwise the scss style do not apply to svg content
  // (see https://stackoverflow.com/questions/36214546/styles-in-component-for-d3-js-do-not-show-in-angular-2/36214723#36214723)
  encapsulation: ViewEncapsulation.None,
  templateUrl: './time-series-line-chart.component.html',
  styleUrls: ['./time-series-line-chart.component.scss']
})
export class TimeSeriesLineChartComponent implements AfterContentInit, OnChanges {

  @Input()
  timeSeriesResults: EventResultData;

  @ViewChild('svg')
  svgElement: ElementRef;

  public ngxSmartModalService;

  dataTrimLabels: { [key: string]: string } = {};
  dataMaxValues: { [key: string]: number } = {};

  stepForInputFields: { [key: string]: number } = {};
  minInputFieldsMax: { [key: string]: number } = {};
  maxInputFieldsMin: { [key: string]: number } = {};
  maxInputFieldsMax: { [key: string]: number } = {};
  minInput: { [key: string]: { [key: string]: number } } = {min: {}, max: {}};
  maxInput: { [key: string]: { [key: string]: number } } = {min: {}, max: {}};

  selectedTrimValues: { [key: string]: { [key: string]: number } } = {min: {}, max: {}};

  private _resizeTimeoutId: number;

  constructor(private lineChartService: LineChartService,
              private spinnerService: SpinnerService,
              private translateService: TranslateService,
              ngxSmartModalService: NgxSmartModalService) {
    this.ngxSmartModalService = ngxSmartModalService;
  }

  ngAfterContentInit(): void {
    this.lineChartService.initChart(this.svgElement, () => this.handlePointSelectionError());
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.redraw();
    this.setDataTrimSettings();
  }

  @HostListener('window:resize', ['$event'])
  windowIsResized(): void {
    this.lineChartService.startResize(this.svgElement);

    // Wait until the resize is done before redrawing the chart
    clearTimeout(this._resizeTimeoutId);
    this._resizeTimeoutId = window.setTimeout(() => {
      this.lineChartService.resizeChart(this.svgElement);
      this.redraw();

      this.lineChartService.endResize(this.svgElement);
    }, 500);
  }

  redraw(): void {
    if (this.timeSeriesResults == null) {
      this.spinnerService.showSpinner('time-series-line-chart-spinner');
      return;
    }
    this.spinnerService.hideSpinner('time-series-line-chart-spinner');

    this.lineChartService.drawLineChart(this.timeSeriesResults, this.selectedTrimValues);
  }

  handlePointSelectionError(): void {
    this.ngxSmartModalService.open('pointSelectionErrorModal');
  }

  redrawChartWithTrimmedData(measurandGroup: string): void {
    this.validateInputValues(measurandGroup);

    this.minInput.max[measurandGroup] =
      Math.min(this.maxInputFieldsMax[measurandGroup], this.selectedTrimValues.max[measurandGroup]);
    this.maxInput.min[measurandGroup] = this.selectedTrimValues.min[measurandGroup] || 0;

    this.redraw();
  }

  validateInputValues(measurandGroup: string): void {
    this.validateInputValuesByType('min', this.minInput, measurandGroup);
    this.validateInputValuesByType('max', this.maxInput, measurandGroup);
  }

  private setDataTrimSettings(): void {
    this.dataTrimLabels = this.lineChartService.dataTrimLabels;
    this.dataMaxValues = this.lineChartService.dataMaxValues;

    Object.keys(this.dataMaxValues).forEach((measurandGroup: string) => {
      this.stepForInputFields[measurandGroup] = this.getAdequateStep(this.dataMaxValues[measurandGroup]);
      this.minInput.min[measurandGroup] = 0;
      this.maxInput.max[measurandGroup] = this.stepForInputFields[measurandGroup] *
        Math.ceil(this.dataMaxValues[measurandGroup] / this.stepForInputFields[measurandGroup]);
      this.minInput.max[measurandGroup] =
        Math.min(this.maxInput.max[measurandGroup], this.selectedTrimValues.max[measurandGroup]);
      this.maxInput.min[measurandGroup] = Math.max(this.minInput.min[measurandGroup], this.selectedTrimValues.min[measurandGroup]);
    });
  }

  private getAdequateStep(maximumValue: number): number {
    const orderOfMagnitudeOfMaxValue: number = Math.max(Math.floor(Math.log(maximumValue) * Math.LOG10E), 0);
    return 5 * Math.pow(10, orderOfMagnitudeOfMaxValue - 2);
  }

  private validateInputValuesByType(type: string, input: { [key: string]: { [key: string]: number } }, measurandGroup: string): void {
    if (this.selectedTrimValues[type][measurandGroup] === null || this.selectedTrimValues[type][measurandGroup] === undefined) {
      return;
    }

    if (this.selectedTrimValues[type][measurandGroup] < input.min[measurandGroup]) {
      this.selectedTrimValues[type][measurandGroup] = input.min[measurandGroup];
    }
    if (this.selectedTrimValues[type][measurandGroup] > input.max[measurandGroup]) {
      this.selectedTrimValues[type][measurandGroup] = input.max[measurandGroup];
    }
  }
}
