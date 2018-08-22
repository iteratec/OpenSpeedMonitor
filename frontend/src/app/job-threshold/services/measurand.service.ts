import {Injectable} from "@angular/core";
import {Measurand} from "../models/measurand.model";
import {Threshold} from "../models/threshold.model";

@Injectable({
  providedIn: 'root'
})

export class MeasurandService {

  measurands: Measurand[] = [];

  constructor() {
  }

  setActualMeasurands(measurands: Measurand[]) {
    this.measurands = measurands.map(measurand => {
      return {...measurand, translationsKey: "frontend.de.iteratec.isr.measurand." + measurand.name};
    })
  }

  getUnusedMeasurands(thresholds: Threshold[]) {
    if (!thresholds) {
      return;
    }
    return this.measurands.filter(measurand =>
      !thresholds.some(threshold => threshold.measurand.name == measurand.name)
    );
  }
}
