import {Injectable} from '@angular/core';
import {Measurand} from '../models/measurand.model';
import {Threshold} from '../models/threshold.model';
import {ThresholdRestService} from './threshold-rest.service';

@Injectable({
  providedIn: 'root'
})

export class MeasurandService {
  private measurands: Measurand[] = [];

  constructor(private thresholdRestService: ThresholdRestService) {
  }

  fetchMeasurands() {
    this.thresholdRestService.getMeasurands().subscribe((measurands: Measurand[]) => {
      this.measurands = measurands.map(measurand => ({
        ...measurand,
        translationsKey: 'frontend.de.iteratec.isr.measurand.' + measurand.name
      }));
    });
  }

  getUnusedMeasurands(thresholds: Threshold[]) {
    if (!thresholds) {
      return;
    }
    return this.measurands.filter(measurand => !thresholds.some(threshold => threshold.measurand.name == measurand.name));
  }
}
