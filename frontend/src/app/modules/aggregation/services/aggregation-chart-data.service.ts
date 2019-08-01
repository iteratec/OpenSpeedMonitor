import { Injectable } from '@angular/core';
import {scaleOrdinal} from "d3-scale";
import {Data} from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class AggregationChartDataService {

  allMeasurandDataMap = {};
  filterRules = [];
  selectedFilter:string = "desc";
  aggregationValue:string = "avg";
  i18nMap = {};
  series = [];

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


  constructor() {}

  public setData(data: any): any{
    this.aggregationValue = data.aggregationValue !== undefined ? data.aggregationValue: this.aggregationValue;
    this.filterRules = data.filterRules;
    this.selectedFilter = data.selectedFilter !== undefined ? data.selectedFilter: this.selectedFilter;
    this.i18nMap = data.i18nMap;
    this.series = data.series;
    this.allMeasurandDataMap = this.getMeasurandDataMap(data.series);
  }

  public getMeasurandDataMap(series){
    let measurands = [];
    let measurandDataMap:{[k: string]: any} = {};

    if(series) {
      series.forEach((serie) =>{
          serie.sideLabel = this.setDataForSideLabel(series, serie);
      });

      measurands = series.map(x => x.measurand).filter((v, i, a) => a.indexOf(v) === i);
      measurands.map(measurand => {
        measurandDataMap[measurand] = {
          measurand: measurand,
          series: series.filter(serie => serie.measurand === measurand)
        };
      });
      Object.keys(measurandDataMap).forEach(k => {
        let measurandData = measurandDataMap[k];
        let firstSerie = measurandData.series[0];
        measurandData.label = firstSerie.measurandLabel;
        measurandData.measurandGroup = firstSerie.measurandGroup;
        measurandData.unit= firstSerie.unit;
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

          measurandData.color = colorScales[unit](measurands.indexOf(k));
        }
        measurandData.highlighted = false;
        measurandData.selected = false;
      });
    }
    return measurandDataMap;
  }

  public getDataForScoreBar(){
    let barsToRender = [];
    let minValue = 0;
    let maxValue = 0;
    let dataForScoreBar:{ min: number, max: number, barsToRender: Array<any> } = {min: 0 ,max: 0 ,barsToRender: []};
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
      dataForScoreBar.min = minValue;
      dataForScoreBar.max = maxValue;

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
      dataForScoreBar.barsToRender = barsToRender;
    }
    return dataForScoreBar;
  }

  public getDataForLegend(){
    return Object.keys(this.allMeasurandDataMap).map(measurand => {
      let measurandData = this.allMeasurandDataMap[measurand];
        return {
          measurand: measurand,
          color: measurandData.color,
          label: measurandData.label,
          highlighted: false,
          selected: false,
          anySelected: false,
          anyHighlighted: false
        }
    });
  }

  getColorscaleForMeasurandGroup(measurandUnit, skipFirst) {
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

  private createDomain(arrayLength) {
    var array = [];
    for (let i = 0; i < arrayLength; i++) {
      array.push(i);
    }
    return array;
  }

  public getDataForHeader(){
    let dataForBars = this.getDataForLabels();
    let header ="";
    let aggregation = "";
    let pages = dataForBars.map(x => x.page).filter((el, i, a) => i === a.indexOf(el));
    let jobGroups = dataForBars.map(x => x.jobGroup).filter((el, i, a) => i === a.indexOf(el));
    this.aggregationValue ==='avg'? aggregation ="Average" : aggregation ="Percentile"+ this.aggregationValue + "%";

      if (pages.length > 1 && jobGroups.length > 1) {
        header = aggregation;
      } else if (pages.length > 1 && jobGroups.length === 1) {
        header = jobGroups[0] + " - " + aggregation;
      } else if (jobGroups.length > 1 && pages.length === 1) {
        header = pages[0] + " - " + aggregation;
      } else if(pages.length ===1&&jobGroups.length===1){
      header = jobGroups[0] + ", " + pages[0] + " - " + aggregation;
      }
    return header;
  }

  public setDataForSideLabel(series, serie){
    let pages = series.map(x => x.page).filter((el, i, a) => i === a.indexOf(el));
    let jobGroups = series.map(x => x.jobGroup).filter((el, i, a) => i === a.indexOf(el));
    if (pages.length > 1 && jobGroups.length > 1) {
     return  '' + serie.page +', ' + serie.jobGroup;
    } else if (pages.length > 1 && jobGroups.length === 1) {
      return  serie.page;
    } else if (jobGroups.length > 1 && pages.length === 1) {
      return serie.jobGroup;
    } else if(pages.length ===1&&jobGroups.length===1){
      return ''
    }
  }

  public getUniqueSideLabels() : String[]{
    return this.getDataForLabels().map(x => x.sideLabel).filter((el, i, a) => i === a.indexOf(el));
  }

  public createEmptyBarsForMissingData(data): any{
    let sideLabels = this.getUniqueSideLabels();
    let sideLabelsForMeasurand =[];
    Object.keys(data).forEach((measurand) => {
      if (sideLabels.length === data[measurand].series.length) {
        return data;
      } else if(sideLabels.length > data[measurand].series.length){
        sideLabelsForMeasurand = data[measurand].series.map(x => x.sideLabel);
        sideLabels.forEach((label) =>{
          if(!sideLabelsForMeasurand.includes(label)){

            data[measurand].series.push({sideLabel: label, value: null});
          }
        });
      }
    });
    return data;
  }

  private getDataForLabels(){
    let dataForLabels = [];
    Object.keys(this.allMeasurandDataMap).forEach(measurand => {
      let measurandData = this.allMeasurandDataMap[measurand];
      measurandData.series.forEach(serie => {
        dataForLabels.push({
          color: measurandData.color,
          value: serie.value,
          measurand: measurand,
          jobGroup: serie.jobGroup,
          page: serie.page,
          measurandGroup: measurandData.measurandGroup,
          pageAndJobGroup: '' + serie.page +', ' + serie.jobGroup,
          sideLabel: serie.sideLabel
        });
      });
    });
    return dataForLabels;
  }

}
