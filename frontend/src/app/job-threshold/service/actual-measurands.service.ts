import { Injectable } from '@angular/core';
import {Measurand} from './model/measurand.model'
import {Threshold} from "./model/threshold.model";
import {log} from "util";


@Injectable({
  providedIn: 'root'
})
export class ActualMeasurandsService {

  measurandList: Measurand[];
  actualMeasurandList: Measurand[];
  constructor() {}

  setActualMeasurands(measurands: Measurand[]) {
    this.measurandList = measurands;
    console.log("ACTUAL SEVICE this.measurandList" + JSON.stringify(this.measurandList));
  }

  getActualMeasurands(thresholds: Threshold[]) {
    this.actualMeasurandList = [];
    console.log("SERVICE thresholds: " + JSON.stringify(thresholds));
    this.measurandList.map( measurand => {
        if (thresholds.map(threshold => threshold.measurand.name).indexOf(measurand.name) == -1) {
          this.actualMeasurandList.push(measurand);
        }
      });







          //.indexOf(name) == -1)



      /*if(this.measurandList.map(element => element.name).indexOf(name) !== -1 ) {
        this.measurandList.splice(this.measurandList.map(element => element.name).indexOf(name) , 1);
      }*/


    return this.actualMeasurandList
  }
}
