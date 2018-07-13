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
  }

  getActualMeasurands(thresholds: Threshold[]) {
    this.actualMeasurandList = [];
    this.measurandList.map( measurand => {
        if (thresholds.map(threshold => threshold.measurand.name).indexOf(measurand.name) == -1) {
          this.actualMeasurandList.push(measurand);
        }
      });
    //this.actualMeasurandList.length < 1 ? this.shouldDisabled = false : this.shouldDisabled = true;

    return this.actualMeasurandList
  }
}
