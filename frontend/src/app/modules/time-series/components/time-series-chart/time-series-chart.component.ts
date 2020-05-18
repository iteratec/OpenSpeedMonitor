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
  templateUrl: './time-series-chart.component.html',
  styleUrls: ['./time-series-chart.component.scss']
})
export class TimeSeriesChartComponent implements AfterContentInit, OnChanges {

  @Input()
  timeSeriesResults: EventResultData;

  @ViewChild('svg')
  svgElement: ElementRef;

  public ngxSmartModalService;

  dataTrimLabels: { [key: string]: string } = {};
  dataTrimInputRange: { [key: string]: { [key: string]: number } } = {min: {}, max: {}};
  dataTrimInputStep: { [key: string]: number } = {};
  maxInputTmpRange: { [key: string]: boolean } = {};

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
    this.redraw(true);
    this.setDataTrimSettings();
  }

  @HostListener('window:resize', ['$event'])
  windowIsResized(): void {
    this.lineChartService.startResize(this.svgElement);

    // Wait until the resize is done before redrawing the chart
    clearTimeout(this._resizeTimeoutId);
    this._resizeTimeoutId = window.setTimeout(() => {
      this.lineChartService.resizeChart(this.svgElement);
      this.redraw(false);

      this.lineChartService.endResize(this.svgElement);
    }, 500);
  }

  redraw(updateAllData: boolean): void {
    if (this.timeSeriesResults == null) {
      this.spinnerService.showSpinner('time-series-line-chart-spinner');
      return;
    }
    this.spinnerService.hideSpinner('time-series-line-chart-spinner');

    this.lineChartService.drawLineChart(this.timeSeriesResults, this.selectedTrimValues, updateAllData);
  }

  handlePointSelectionError(): void {
    this.ngxSmartModalService.open('pointSelectionErrorModal');
  }

  validateInputValuesByEvent(event, selectedInput: string, otherInput: string, measurandGroup: string): void {
    if (event.inputType !== 'insertText' && !event.inputType.startsWith('delete')) {
      this.considerMaxInputTmpRange(selectedInput, measurandGroup);
      this.validateInputValues(selectedInput, otherInput, measurandGroup);
    }
  }

  validateInputValues(selectedInput: string, otherInput: string, measurandGroup: string): void {
    if (
      this.selectedTrimValues.min[measurandGroup] &&
      this.selectedTrimValues.max[measurandGroup] &&
      this.selectedTrimValues.min[measurandGroup] >= this.selectedTrimValues.max[measurandGroup]
    ) {
      this.selectedTrimValues[otherInput][measurandGroup] = selectedInput === 'min' ?
        this.selectedTrimValues[selectedInput][measurandGroup] + this.dataTrimInputStep[measurandGroup] :
        this.selectedTrimValues[selectedInput][measurandGroup] - this.dataTrimInputStep[measurandGroup];
    }

    const inputRangeMin = this.selectedTrimValues.min[measurandGroup] ?
      Math.max(this.dataTrimInputRange.min[measurandGroup], this.selectedTrimValues.min[measurandGroup]) :
      this.dataTrimInputRange.min[measurandGroup];

    const inputRangeMax = this.selectedTrimValues.max[measurandGroup] ?
      Math.min(this.dataTrimInputRange.max[measurandGroup], this.selectedTrimValues.max[measurandGroup]) :
      this.dataTrimInputRange.max[measurandGroup];

    this.validateInputValuesByType(otherInput, inputRangeMin, inputRangeMax, measurandGroup);
    this.maxInputTmpRange[measurandGroup] = this.selectedTrimValues.min[measurandGroup] &&
      (this.selectedTrimValues.max[measurandGroup] === undefined || this.selectedTrimValues.max[measurandGroup] === null);
  }

  private setDataTrimSettings(): void {
    this.dataTrimLabels = this.lineChartService.dataTrimLabels;
    const dataMaxValues = this.lineChartService.dataMaxValues;

    Object.keys(dataMaxValues).forEach((measurandGroup: string) => {
      this.dataTrimInputStep[measurandGroup] = this.getAdequateStep(dataMaxValues[measurandGroup]);
      this.dataTrimInputRange.min[measurandGroup] = 0;
      this.dataTrimInputRange.max[measurandGroup] = this.dataTrimInputStep[measurandGroup] *
        Math.ceil(dataMaxValues[measurandGroup] / this.dataTrimInputStep[measurandGroup]);
    });
  }

  private getAdequateStep(maximumValue: number): number {
    const orderOfMagnitudeOfMaxValue: number = Math.max(Math.floor(Math.log(maximumValue) * Math.LOG10E), 0);
    return 5 * Math.pow(10, orderOfMagnitudeOfMaxValue - 2);
  }

  private validateInputValuesByType(type: string, inputRangeMin: number, inputRangeMax: number, measurandGroup: string): void {
    if (this.selectedTrimValues[type][measurandGroup] === null || this.selectedTrimValues[type][measurandGroup] === undefined) {
      return;
    }

    if (this.selectedTrimValues[type][measurandGroup] < inputRangeMin) {
      this.selectedTrimValues[type][measurandGroup] = inputRangeMin;
    }
    if (this.selectedTrimValues[type][measurandGroup] > inputRangeMax) {
      this.selectedTrimValues[type][measurandGroup] = inputRangeMax;
    }
  }

  private considerMaxInputTmpRange(selectedInput: string, measurandGroup: string): void {
    if (selectedInput === 'max' && this.maxInputTmpRange[measurandGroup]) {
      this.selectedTrimValues.max[measurandGroup] = this.selectedTrimValues.min[measurandGroup] + this.dataTrimInputStep[measurandGroup];
    }
  }
}
