import {Injectable} from "@angular/core";
import {Measurand} from "../models/measurand.model";
import {Threshold} from "../models/threshold.model";

@Injectable({
  providedIn: 'root'
})

export class MeasurandService {

  measurandList: Measurand[] = [];
  actualMeasurandList: Measurand[] = [];

  constructor() {}

  setActualMeasurands(measurands: Measurand[]) {
    /*Format measurands name*/

    this.measurandList = measurands;
    this.measurandList.map(measurand => {
      measurand.translationsKey = "frontend.de.iteratec.isr.measurand." + measurand.name;
    })
  }


  getActualMeasurands(thresholds: Threshold[]) {
    if (!thresholds) {
      return;
    }
    this.actualMeasurandList = [];
    this.measurandList.map( measurand => {
        if (thresholds.map(threshold => threshold.measurand.name).indexOf(measurand.name) == -1) {
          this.actualMeasurandList.push(measurand);
        }
      });
    return this.actualMeasurandList
  }
}
