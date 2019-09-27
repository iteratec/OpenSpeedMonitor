import { Injectable } from '@angular/core';
import {scaleOrdinal} from "d3-scale";
import {URL} from "../../../enums/url.enum";
import {BarchartDataService} from "./barchart-data.service";
import {BehaviorSubject, combineLatest, Subject} from "rxjs";
import {ResultSelectionCommand} from "../../result-selection/models/result-selection-command.model";
import {RemainingResultSelection} from "../../result-selection/models/remaing-result-selection.model";
import {AggregationChartDataByMeasurand} from "../models/aggregation-chart-data.model";
import {AggregationChartSeries} from "../models/aggregation-chart-series.model";
import {SpinnerService} from "../../shared/services/spinner.service";
import {ActivatedRoute, Params, Router} from "@angular/router";
import {ResultSelectionStore} from "../../result-selection/services/result-selection.store";
import {take, takeUntil} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class AggregationChartDataService {

  allMeasurandDataMap: AggregationChartDataByMeasurand = {};
  filterRules  = {};
  i18nMap: {comparativeDeterioration:string, comparativeImprovement: string, jobGroup: string, measurand: string, page: string};
  series: AggregationChartSeries[] = [];
  selectedFilter:string = 'asc';
  aggregationValue:string = 'avg';
  percentileValue:number = 50;
  stackBars:boolean = true;
  dataForScoreBar:{ min: number, max: number, barsToRender: Array<any> } = {min: 0 ,max: 0 ,barsToRender: []};
  dataForHeader:string = '';
  uniqueSideLabels:string[] = [];
  aggregationType:string = 'avg';
  hasComparativeData: boolean = false;

  loadingTimeColors:Array<string> = [
    "#1660A7",
    "#558BBF",
    "#95b6d7",
    "#d4e2ef"
  ];
  countOfRequestColors:Array<string> = [
    "#E41A1C",
    "#eb5859",
    "#f29697",
    "#fad5d5"
  ];
  sizeOfRequestColors:Array<string> = [
    "#F18F01",
    "#f4ad46",
    "#f8cc8b",
    "#fcead0"
  ];
  csiColors:Array<string> = [
    "#59B87A",
    "#86cb9e",
    "#b3dec2",
    "#e0f2e6"
  ];

  trafficColors = [
    "#5cb85c",
    "#f0ad4e",
    "#d9534f"
  ];

  measurandOrder: string[] = [
    "CS_BY_WPT_VISUALLY_COMPLETE",
    "CS_BY_WPT_DOC_COMPLETE",
    "FULLY_LOADED_TIME",
    "VISUALLY_COMPLETE",
    "VISUALLY_COMPLETE_99",
    "VISUALLY_COMPLETE_95",
    "VISUALLY_COMPLETE_90",
    "VISUALLY_COMPLETE_85",
    "CONSISTENTLY_INTERACTIVE",
    "FIRST_INTERACTIVE",
    "SPEED_INDEX",
    "DOC_COMPLETE_TIME",
    "LOAD_TIME",
    "START_RENDER",
    "DOM_TIME",
    "FIRST_BYTE",
    "FULLY_LOADED_INCOMING_BYTES",
    "DOC_COMPLETE_INCOMING_BYTES",
    "FULLY_LOADED_REQUEST_COUNT",
    "DOC_COMPLETE_REQUESTS"
  ];

  speedIndexColors:Array<string> = this.loadingTimeColors;

  measurandGroupColorCombination = {
    "ms": this.loadingTimeColors,
    "s": this.loadingTimeColors,
    "#": this.countOfRequestColors,
    "KB": this.sizeOfRequestColors,
    "MB": this.sizeOfRequestColors,
    "%": this.csiColors,
    "": this.speedIndexColors
  };

  barchartAverageData$: BehaviorSubject<any> = new BehaviorSubject<any>([]);
  barchartMedianData$: BehaviorSubject<any> = new BehaviorSubject<any>([]);
  ascSelected:boolean = true;
  descSelected:boolean = false;

  constructor(private barchartDataService: BarchartDataService,
              private route: ActivatedRoute,
              private router: Router,
              private resultSelectionStore: ResultSelectionStore,
              private spinnerService: SpinnerService) {
    route.queryParams.subscribe((params: Params) => {
      this.selectedFilter = params.selectedFilter ? params.selectedFilter : this.selectedFilter;
      this.aggregationType = params.selectedAggregationValue ? params.selectedAggregationValue : this.aggregationType;
      this.stackBars = params.stackBars == 1;
      this.percentileValue = params.selectedPercentile ? parseInt(params.selectedPercentile) : this.percentileValue;
    });
  }

  getBarchartData(resultSelectionCommand: ResultSelectionCommand,remainingResultSelection: RemainingResultSelection): void {
    this.spinnerService.showSpinner('aggregation-chart-spinner');
    const finishedLoadingAvg$: Subject<void> = new Subject<void>();
    const finishedLoadingPercentile$: Subject<void> = new Subject<void>();

    const additionalParams: Params = {
      selectedFilter: this.selectedFilter,
      selectedAggregationValue: this.aggregationType,
      selectedPercentile: this.percentileValue,
      stackBars: this.stackBars ? 1 : 0
    };

    this.resultSelectionStore.writeQueryParams(additionalParams);

    combineLatest(finishedLoadingAvg$, finishedLoadingPercentile$).subscribe(() => {
      this.spinnerService.hideSpinner('aggregation-chart-spinner');
    });

    this.barchartDataService.fetchBarchartData<any>(
      resultSelectionCommand,
      remainingResultSelection,
      "avg",
      URL.AGGREGATION_BARCHART_DATA
    ).subscribe(result => {
      this.barchartAverageData$.next(this.sortDataByMeasurandOrder(result));
      finishedLoadingAvg$.next();
    });

    this.barchartDataService.fetchBarchartData<any>(
      resultSelectionCommand,
      remainingResultSelection,
      this.percentileValue,
      URL.AGGREGATION_BARCHART_DATA
    ).subscribe(result => {
      this.barchartMedianData$.next(this.sortDataByMeasurandOrder(result));
      finishedLoadingPercentile$.next();
    });
  }

  reloadPercentile(percentile: number,resultSelectionCommand: ResultSelectionCommand, remainingResultSelection: RemainingResultSelection): void {
    this.percentileValue = percentile;
    this.barchartDataService.fetchBarchartData<any>(
      resultSelectionCommand,
      remainingResultSelection,
      this.percentileValue,
      URL.AGGREGATION_BARCHART_DATA
    ).subscribe(result => {
      this.barchartMedianData$.next(this.sortDataByMeasurandOrder(result));
    });
  }

  public setData(): void {
    let data;
    if(this.aggregationType==='percentile'){
      data = this.barchartMedianData$.getValue();
    }else{
      data = this.barchartAverageData$.getValue();
    }
    this.filterRules = data.filterRules;
    this.i18nMap = data.i18nMap;
    this.aggregationValue = data.series[0].aggregationValue !== undefined ? data.series[0].aggregationValue : this.aggregationValue;
    this.series = data.series;
    this.hasComparativeData = data.hasComparativeData;
    this.setMeasurandDataMap(data.series);
    Object.keys(this.allMeasurandDataMap).forEach((measurand) => {
      this.sortSeriesByFilterRule(this.allMeasurandDataMap[measurand]);
    });
    this.setDataForScoreBar();
    this.setDataForHeader();
    this.setUniqueSideLabels();
    if(!this.hasComparativeData) {
      this.createEmptyBarsForMissingData();
    }
  }

  public setMeasurandDataMap(series: AggregationChartSeries[]): void{
    let measurands: string[] = [];
    let measurandDataMap: AggregationChartDataByMeasurand = {};
    if(series) {
      if(this.hasComparativeData){
        series = this.setComparativeData(series);
      }
      series.forEach(datum => {
        datum.sideLabel = this.setDataForSideLabel(series, datum);
      });

      measurands = series.map(x => x.measurand).filter((v, i, a) => a.indexOf(v) === i);
      measurands.map(measurand => {
        measurandDataMap[measurand] = {
          measurand: measurand,
          series: series.filter(datum => datum.measurand === measurand),
          aggregationValue: '',
          label: '',
          measurandGroup: '',
          unit: '',
          highlighted: false,
          selected: false,
          hasComparative: false,
          color: '',
        };
      });

      Object.keys(measurandDataMap).forEach(measurand => {
        let measurandData = measurandDataMap[measurand];
        let firstSerie = measurandData.series[0];
        measurandData.aggregationValue = firstSerie.aggregationValue;
        measurandData.label = firstSerie.measurandLabel;
        measurandData.measurandGroup = firstSerie.measurandGroup;
        measurandData.unit= firstSerie.unit;
        measurandData.highlighted = false;
        measurandData.selected = false;
        measurandData.hasComparative = this.hasComparativeData;
        measurandData.isImprovement = firstSerie.isImprovement;
        measurandData.isDeterioration = firstSerie.isDeterioration;

        if(measurandData.isImprovement || measurandData.isDeterioration){
          let color = this.getColorscaleForTrafficlight()(measurandData.isImprovement ? "good" : "bad");
          measurandData.color = color.toString();
        } else {
          let unit = measurandData.unit;
          let colorScales = {};
          colorScales[unit] = colorScales[unit] || this.getColorscaleForMeasurandGroup(unit, this.hasComparativeData);
          measurandData.color = colorScales[unit](measurands.indexOf(measurand));
        }
      });
    }
    this.allMeasurandDataMap = measurandDataMap;
  }

  private sortDataByMeasurandOrder(data){
    if(Object.getOwnPropertyNames(data).length < 1) {
      return data;
    }
    data.series.sort((a, b) => {
      var idxA = this.measurandOrder.indexOf(a.measurand);
      var idxB = this.measurandOrder.indexOf(b.measurand);
      if (idxA < 0) {
        return (idxB < 0) ? 0 : 1;
      }
      return (idxB < 0) ? -1 : (idxA - idxB);
    });
    return data;
  }

  private sortSeriesByFilterRule(data): void {
    if(this.selectedFilter ==='desc'){
      this.ascSelected = false;
      this.descSelected = true;
      Object.keys(this.filterRules).forEach((key)=> this.filterRules[key].selected = false);
      data.series = data.series.sort((a, b) => (a.value > b.value) ? -1 : ((b.value > a.value) ? 1 : 0));
    }else if (Object.keys(this.filterRules).includes(this.selectedFilter)) {
      this.ascSelected = false;
      this.descSelected = false;
      Object.keys(this.filterRules).forEach((key)=> key === this.selectedFilter ? this.filterRules[key].selected = true : this.filterRules[key].selected = false);
      let keyForFilterRule = Object.keys(this.filterRules).filter((key) => key === this.selectedFilter).toString();
      let filterRule = this.filterRules[keyForFilterRule];
      data.series = data.series.filter(datum => filterRule.some(x => datum.jobGroup === x.jobGroup && datum.page === x.page));
    }else{
      this.selectedFilter = 'asc';
      this.ascSelected = true;
      this.descSelected = false;
      Object.keys(this.filterRules).forEach((key)=> this.filterRules[key].selected = false);
      data.series = data.series.sort((a, b) => (a.value < b.value) ? -1 : ((b.value < a.value) ? 1 : 0));
    }
  }

  public setDataForScoreBar(): void {
    let barsToRender = [];
    let minValue = 0;
    let maxValue = 0;
    const availableScoreBars = [
      {
        id: "good",
        fill: "#bbe2bb",
        label: "GOOD",
        cssClass: "d3chart-good",
        end: 1000,
        start: 0
      },
      {
        id: "okay",
        fill: "#f9dfba",
        label: "OK",
        cssClass: "d3chart-okay",
        end: 3000,
        start: 0

      },
      {
        id: "bad",
        fill: "#f5d1d0",
        label: "BAD",
        cssClass: "d3chart-bad",
        end: undefined,
        start: 0
      }
    ];
    let values = [];
    if(this.allMeasurandDataMap) {
      Object.keys(this.allMeasurandDataMap).forEach(k => {
        values = values.concat(this.allMeasurandDataMap[k].series.map(x=>x.value));
      });
      minValue = values.length > 0 ? Math.min(Math.min.apply(null, values), 0) : 0;
      maxValue = values.length > 0 ? Math.max(Math.max.apply(null, values), 0) : 0;
      this.dataForScoreBar.min = minValue;
      this.dataForScoreBar.max = maxValue;

      let lastBarEnd: number = 0;
      for (let curScoreBar = 0; curScoreBar < availableScoreBars.length; curScoreBar++) {
        let bar = availableScoreBars[curScoreBar];
        barsToRender.push(bar);
        bar.start = lastBarEnd;
        if (!bar.end || maxValue < bar.end || !availableScoreBars[curScoreBar + 1]) {
          bar.end = maxValue;
          break;
        }
        lastBarEnd = bar.end;
      }
      barsToRender.reverse();
      this.dataForScoreBar.barsToRender = barsToRender;
    }
  }

  public setDataForHeader(): void {
    let data = this.getDataForLabels();
    let header ="";
    let aggregation = "";
    let pages = data.map(x => x.page).filter((el, i, a) => i === a.indexOf(el));
    let jobGroups = data.map(x => x.jobGroup).filter((el, i, a) => i === a.indexOf(el));
    this.aggregationValue ==='avg'? aggregation ="Average" : aggregation = `Percentile ${this.aggregationValue}%`;

      if (pages.length > 1 && jobGroups.length > 1) {
        header = aggregation;
      } else if (pages.length > 1 && jobGroups.length === 1) {
        header = `${jobGroups[0]} - ${aggregation}`;
      } else if (jobGroups.length > 1 && pages.length === 1) {
        pages[0] !== null? header = `${pages[0]} - ${aggregation}` : header = aggregation;
      } else if(pages.length ===1 && jobGroups.length===1){
        pages[0] !== null? header = `${jobGroups[0]}, ${pages[0]} - ${aggregation}` : header = `${jobGroups[0]} - ${aggregation}`;
      }
      this.dataForHeader = header;
  }

  public setUniqueSideLabels(): void {
    this.uniqueSideLabels =  this.getDataForLabels().map(x => x.sideLabel).filter((el, i, a) => i === a.indexOf(el));
  }

  public createEmptyBarsForMissingData(): void {
    let sideLabelsForMeasurand: string[] =[];
    Object.keys(this.allMeasurandDataMap).forEach((measurand) => {
      let data = this.allMeasurandDataMap[measurand];
      if (this.uniqueSideLabels.length === data.series.length) {
        return this.allMeasurandDataMap;
      } else if(this.uniqueSideLabels.length > data.series.length){
        sideLabelsForMeasurand = data.series.map(x => x.sideLabel);
        this.uniqueSideLabels.forEach((label) =>{
          if(!sideLabelsForMeasurand.includes(label)){
            data.series.push({aggregationValue: data.aggregationValue, sideLabel: label, value: null, measurand: measurand, jobGroup: null, page:null, measurandGroup:data.measurandGroup, measurandLabel:data.label, unit:data.unit});
          }
        });
      }
    });
  }


  getColorscaleForMeasurandGroup(measurandUnit: string, skipFirst: boolean) {
    let colors = this.measurandGroupColorCombination[measurandUnit].slice(skipFirst ? 1 : 0);
    return scaleOrdinal()
      .domain(this.createDomain(colors.length))
      .range(colors);
  }

  getColorscaleForTrafficlight() {
    return scaleOrdinal()
      .domain(["good", "ok", "bad"] as ReadonlyArray<string>)
      .range(this.trafficColors);
  }

  createDomain(arrayLength: number): ReadonlyArray<string> {
    var array = [];
    for (let i = 0; i < arrayLength; i++) {
      array.push(i);
    }
    return array as ReadonlyArray<string>;
  }

  public setComparativeData(series: AggregationChartSeries[]){
    let comparativeData: AggregationChartSeries[] = [];
    series.forEach(datum => {
      let difference = datum.value - datum.valueComparative;
      let isImprovement = (datum.measurandGroup === "PERCENTAGES") ? difference > 0 : difference < 0;
      let measurandSuffix = isImprovement ? "improvement" : "deterioration";
      let label = isImprovement ? (this.i18nMap.comparativeImprovement || "improvement") : (this.i18nMap.comparativeDeterioration || "deterioration");
      comparativeData.push({
        measurand: datum.measurand + "_" + measurandSuffix,
        aggregationValue: datum.aggregationValue,
        jobGroup: datum.jobGroup,
        measurandGroup: datum.measurandGroup,
        measurandLabel: label,
        page: datum.page,
        value: difference,
        unit: datum.unit,
        sideLabel: datum.sideLabel,
        browser: datum.browser,
        deviceType: datum.deviceType,
        operatingSystem: datum.operatingSystem,
        isImprovement: isImprovement,
        isDeterioration: !isImprovement
      });
    });
    return series.concat(comparativeData);
  }

  private setDataForSideLabel(series: AggregationChartSeries[], datum: AggregationChartSeries): string {
    let pages = series.map(x => x.page).filter((el, i, a) => i === a.indexOf(el));
    let jobGroups = series.map(x => x.jobGroup).filter((el, i, a) => i === a.indexOf(el));
    let browsers = series.map(x => x.browser).filter((el, i, a) => i === a.indexOf(el));
    let sidelabel: string = "";
    if (pages.length > 1 && jobGroups.length > 1) {
      sidelabel = `${datum.page}, ${datum.jobGroup}`;
    } else if (pages.length > 1 && jobGroups.length === 1) {
      sidelabel =  datum.page;
    } else if (jobGroups.length > 1 && pages.length === 1) {
      sidelabel = datum.jobGroup;
    } else if(pages.length === 1 && jobGroups.length === 1){
      sidelabel = '';
    }
    if(browsers.length > 1) {
      if (sidelabel.length > 0) {
        sidelabel = `${sidelabel}, ${datum.browser}`
      } else if (sidelabel.length === 0) {
        sidelabel = datum.browser
      }
    }

    return sidelabel
  }

  private getDataForLabels(): any[]{
    let dataForLabels = [];
    Object.keys(this.allMeasurandDataMap).forEach(measurand => {
      let measurandData = this.allMeasurandDataMap[measurand];
      measurandData.series.forEach(datum => {
        dataForLabels.push({
          jobGroup: datum.jobGroup,
          page: datum.page,
          sideLabel: datum.sideLabel
        });
      });
    });
    return dataForLabels;
  }
}
