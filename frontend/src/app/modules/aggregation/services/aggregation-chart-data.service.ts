import { Injectable } from '@angular/core';
import {GrailsBridgeService} from "../../../services/grails-bridge.service";

@Injectable({
  providedIn: 'root'
})
export class AggregationChartDataService {

  allMeasurandDataMap = {};
  measurandGroupDataMap = {};
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

  constructor(private grailsBridgeService: GrailsBridgeService) { }

  public setData(data: any): any{
    this.aggregationValue = data.aggregationValue !== undefined ? data.aggregationValue: this.aggregationValue;
    this.filterRules = data.filterRules;
    this.selectedFilter = data.selectedFilter !== undefined ? data.selectedFilter: this.selectedFilter;
    this.i18nMap = data.i18nMap;
    this.allMeasurandDataMap = this.getMeasurandDataMap(data.series);
  }

  public getDataForHeader(){
    let header ="";
    if (this.aggregationValue === 'avg') {
      header = 'Average'
    } else {
      header = "Percentile: " + this.aggregationValue + "%"
    }


    return {
      width: this.chartBarsWidth,
      text: header
    };
  }

  public getDataForBarScore(){
    console.log("barscore");

    return {
      width: this.chartBarsWidth,
      min: 1200,
      max: 1200
    };
  }
  public getDataForLegend(){
    console.log("legend");
    return {
      entries:{
          id: 1,
          color: '#1660A7',
          label: 'label'
      },
      width: this.chartBarsWidth
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
        let firstSerie = measurandDataMap[k].series[0];
        measurandData.label = firstSerie.measurandLabel;
        measurandData.measurandGroup = firstSerie.measurandGroup;
        measurandData.unit= firstSerie.unit;
        measurandData.isDeterioration=firstSerie.isDeterioration;
        measurandData.isImprovement=firstSerie.isImprovement;

        let colorProvider = this.grailsBridgeService.globalOsmNamespace.ChartColorProvider();
        if (measurandData.isImprovement || measurandData.isDeterioration) {
          measurandData.color = colorProvider.getColorscaleForTrafficlight()(measurandData.isImprovement ? "good" : "bad");
        }else{
          let unit = measurandData.unit;
          let colorScales ={};
          //let hasComparative = this.measurandGroupDataMap[measurandData.measurandGroup].hasComparative;
          colorScales[unit] = colorProvider.getColorscaleForMeasurandGroup(unit, false);
          measurandData.color = colorScales[unit](k);
        }
      });
    }
    console.log(measurandDataMap);
    return measurandDataMap;
  }
}
