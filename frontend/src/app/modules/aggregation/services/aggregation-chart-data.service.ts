import { Injectable } from '@angular/core';
import {scaleOrdinal} from "d3-scale";
import {URL} from "../../../enums/url.enum";
import {BarchartDataService} from "./barchart-data.service";
import {BehaviorSubject} from "rxjs";
import {ResultSelectionCommand} from "../../result-selection/models/result-selection-command.model";
import {RemainingResultSelection} from "../../result-selection/models/remaing-result-selection.model";

@Injectable({
  providedIn: 'root'
})
export class AggregationChartDataService {

  allMeasurandDataMap = {};
  filterRules = {};
  dataForChartBars = {};
  i18nMap = {};
  series = [];
  selectedFilter:string = 'asc';
  aggregationValue:string = 'avg';
  percentileValue:number = 50;
  stackBars:boolean = true;
  dataForScoreBar:{ min: number, max: number, barsToRender: Array<any> } = {min: 0 ,max: 0 ,barsToRender: []};
  dataForHeader:string = '';
  uniqueSideLabels:string[];
  aggregationType:string = 'avg';

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

  measurandGroupColorCombination: {} = {
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


  constructor(private barchartDataService: BarchartDataService) {}

  getBarchartData(resultSelectionCommand: ResultSelectionCommand,remainingResultSelection: RemainingResultSelection): void {
    this.barchartDataService.fetchBarchartData<any>(
      resultSelectionCommand,
      remainingResultSelection,
      "avg",
      URL.AGGREGATION_BARCHART_DATA
    ).subscribe(result => {
      this.barchartAverageData$.next(this.sortDataByMeasurandOrder(result));
    });

    this.barchartDataService.fetchBarchartData<any>(
      resultSelectionCommand,
      remainingResultSelection,
      this.percentileValue.toString(),
      URL.AGGREGATION_BARCHART_DATA
    ).subscribe(result => {
      this.barchartMedianData$.next(this.sortDataByMeasurandOrder(result));
    });
  }

  reloadPercentile(percentile: number,resultSelectionCommand: ResultSelectionCommand, remainingResultSelection: RemainingResultSelection): void {
    this.percentileValue = percentile;
    this.barchartDataService.fetchBarchartData<any>(
      resultSelectionCommand,
      remainingResultSelection,
      this.percentileValue.toString(),
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
    this.setMeasurandDataMap(data.series);
    Object.keys(this.allMeasurandDataMap).forEach((measurand) => {
      this.sortSeriesByFilterRule(this.allMeasurandDataMap[measurand]);
    });
    this.setDataForScoreBar();
    this.setDataForHeader();
    this.setUniqueSideLabels();
    this.createEmptyBarsForMissingData(this.allMeasurandDataMap);
  }

  public setMeasurandDataMap(series: any[]): void{
    let measurands = [];
    let measurandDataMap = {};
    if(series) {
      series.forEach((serie) => {
          serie.sideLabel = this.setDataForSideLabel(series, serie);
      });

      measurands = series.map(x => x.measurand).filter((v, i, a) => a.indexOf(v) === i);
      measurands.map(measurand => {
        measurandDataMap[measurand] = {
          measurand: measurand,
          series: series.filter(serie => serie.measurand === measurand)
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
        measurandData.isDeterioration=firstSerie.isDeterioration;
        measurandData.isImprovement=firstSerie.isImprovement;
        measurandData.hasComparative = measurandData.series.some(() => measurandData.isDeterioration|| measurandData.isImprovement);

        if (measurandData.isImprovement || measurandData.isDeterioration) {
          measurandData.color = this.getColorscaleForTrafficlight()(measurandData.isImprovement ? "good" : "bad");
        }else{
          let unit = measurandData.unit;
          let colorScales ={};
          let hasComparative = measurandData.hasComparative;
          colorScales[unit] = colorScales[unit] || this.getColorscaleForMeasurandGroup(unit, hasComparative);
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
      data.series = data.series.filter((serie) => filterRule.some(x => serie.jobGroup === x.jobGroup && serie.page === x.page));
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
        start: undefined
      },
      {
        id: "okay",
        fill: "#f9dfba",
        label: "OK",
        cssClass: "d3chart-okay",
        end: 3000,
        start: undefined

      },
      {
        id: "bad",
        fill: "#f5d1d0",
        label: "BAD",
        cssClass: "d3chart-bad",
        start: undefined
      }
    ];
    let values = [];
    if(this.allMeasurandDataMap) {
      Object.keys(this.allMeasurandDataMap).forEach(k => {
        values = this.allMeasurandDataMap[k].series.map(x=>x.value);
      });
      minValue = values.length > 0 ? Math.min(Math.min.apply(null, values), 0) : 0;
      maxValue = values.length > 0 ? Math.max(Math.max.apply(null, values), 0) : 0;
      this.dataForScoreBar.min = minValue;
      this.dataForScoreBar.max = maxValue;

      let lastBarEnd = 0;
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

  public createEmptyBarsForMissingData(data): void {
    let sideLabelsForMeasurand =[];
    Object.keys(data).forEach((measurand) => {
      if (this.uniqueSideLabels.length === data[measurand].series.length) {
        return data;
      } else if(this.uniqueSideLabels.length > data[measurand].series.length){
        sideLabelsForMeasurand = data[measurand].series.map(x => x.sideLabel);
        this.uniqueSideLabels.forEach((label) =>{
          if(!sideLabelsForMeasurand.includes(label)){
            data[measurand].series.push({sideLabel: label, value: null});
          }
        });
      }
    });
    this.dataForChartBars = data;
  }


  getColorscaleForMeasurandGroup(measurandUnit: string, skipFirst: boolean){
    let colors = this.measurandGroupColorCombination[measurandUnit].slice(skipFirst ? 1 : 0);
    return scaleOrdinal()
      .domain(this.createDomain(colors.length))
      .range(colors);
  }

  getColorscaleForTrafficlight() {
    return scaleOrdinal()
      .domain(["good", "ok", "bad"])
      .range(this.trafficColors);
  }

  createDomain(arrayLength: number){
    var array = [];
    for (let i = 0; i < arrayLength; i++) {
      array.push(i);
    }
    return array;
  }

  private setDataForSideLabel(series, serie): string {
    let pages = series.map(x => x.page).filter((el, i, a) => i === a.indexOf(el));
    let jobGroups = series.map(x => x.jobGroup).filter((el, i, a) => i === a.indexOf(el));
    if (pages.length > 1 && jobGroups.length > 1) {
      return `${serie.page}, ${serie.jobGroup}`;
    } else if (pages.length > 1 && jobGroups.length === 1) {
      return  serie.page;
    } else if (jobGroups.length > 1 && pages.length === 1) {
      return serie.jobGroup;
    } else if(pages.length ===1&&jobGroups.length===1){
      return '';
    }
  }

  private getDataForLabels(): any[]{
    let dataForLabels = [];
    Object.keys(this.allMeasurandDataMap).forEach(measurand => {
      let measurandData = this.allMeasurandDataMap[measurand];
      measurandData.series.forEach(serie => {
        dataForLabels.push({
          jobGroup: serie.jobGroup,
          page: serie.page,
          sideLabel: serie.sideLabel
        });
      });
    });
    return dataForLabels;
  }
}
