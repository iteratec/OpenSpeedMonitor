import { Injectable } from '@angular/core';
import {scaleOrdinal} from "d3-scale";

@Injectable({
  providedIn: 'root'
})
export class AggregationChartDataService {

  allMeasurandDataMap = {};
  sideLabelData = [];
  filterRules = [];
  selectedFilter:string = "desc";
  headerText:string = "";
  aggregationValue:string = "avg";
  comparitiveValue = "";
  dataAvailalbe = false;
  i18nMap = {};
  dataLength:number = 0;
  chartBarsWidth = 2300;
  chartBarsHeight = 230;
  series = [];

  availableScoreBars = [
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
      cssClass: "d3chart-ok",
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
      measurands=series.map(x => x.measurand).filter((v, i, a) => a.indexOf(v) === i);
      measurands.forEach(measurand => {
        measurandDataMap[measurand] = {
          id: measurand,
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
          colorScales[unit] = this.getColorscaleForMeasurandGroup(unit, hasComparative);
          measurandData.color = colorScales[unit](k);
        }
      });
    }
    return measurandDataMap;
  }

  public getDataForScoreBar(){
    let barsToRender = [];
    let minValue = 0;
    let maxValue = 0;
    let dataForScoreBar:{ min: number, max: number, barsToRender: Array<any> } = {min: 0 ,max: 0 ,barsToRender: []};

    let values = [];
    if(this.allMeasurandDataMap) {
      Object.keys(this.allMeasurandDataMap).forEach(k => {
        values = this.allMeasurandDataMap[k].series.map(x=>x.value);
      });
        //let values = data.series.filter(x => x.measurandGroup === "LOAD_TIMES").map(x => x.value);
      minValue = values.length > 0 ? Math.min(Math.min.apply(null, values), 0) : 0;
      maxValue = values.length > 0 ? Math.max(Math.max.apply(null, values), 0) : 0;
      dataForScoreBar.min = minValue;
      dataForScoreBar.max = maxValue;

      let lastBarEnd = 0;
      for (let curScoreBar = 0; curScoreBar < this.availableScoreBars.length; curScoreBar++) {
        let bar = this.availableScoreBars[curScoreBar];
        barsToRender.push(bar);
        bar.start = lastBarEnd;
        if (!bar.end || maxValue < bar.end || !this.availableScoreBars[curScoreBar + 1]) {
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
          label: measurandData.label
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
    let header ="";
    let measurands = Object.keys(this.allMeasurandDataMap);
    if(this.allMeasurandDataMap && measurands.length === 1){
      header = "" + this.allMeasurandDataMap[measurands[0]].jobGroup + "";
    }





    if (this.aggregationValue === 'avg') {
      header = 'Average'
    } else {
      //header = "Percentile: " + this.aggregationValue + "%"
    }


    return {
      width: this.chartBarsWidth,
      text: header
    };
  }



  public getDataForSideLabels(){
    console.log("label");
    return {
      height: this.chartBarsHeight,
      labels: this.sideLabelData
    };
  }

  public getAllMeasurands(){
    return Object.keys(this.allMeasurandDataMap);
  }

  public getDataForBars(){
    console.log("data for bars");
    return {
      id: 1,
      values: 10,
      color: '#1660A7',
      min: 1200,
      max: 1200,
      height: this.chartBarsHeight,
      width: this.chartBarsWidth,
      forceSignInLabel: false
    }
  }


}
