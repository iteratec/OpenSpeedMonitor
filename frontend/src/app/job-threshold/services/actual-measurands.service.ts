import {Injectable} from "@angular/core";
import {Measurand} from "../models/measurand.model";
import {Threshold} from "../models/threshold.model";

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
    return this.actualMeasurandList
  }
}
