import {Injectable} from "@angular/core";
import {MeasuredEvent} from "../models/measured-event.model";
import {ThresholdForJob} from "../models/threshold-for-job.model";

@Injectable({
  providedIn: 'root'
})
export class ActualMeasuredEventsService {

  measuredEventList: MeasuredEvent[];
  actualMeasuredEventList: MeasuredEvent[];

  constructor() { }

  setActualMeasuredEvents(measuredEvents: MeasuredEvent[]) {
    this.measuredEventList = measuredEvents;
  }

  getActualMeasuredEvents(thresholdsforJob: ThresholdForJob[]) {
    this.actualMeasuredEventList = [];
    this.measuredEventList.map( measuredEvent => {
      if (thresholdsforJob.map(thresholdForJob => thresholdForJob.measuredEvent.name).indexOf(measuredEvent.name) == -1) {
        this.actualMeasuredEventList.push(measuredEvent);
      }
    });
    return this.actualMeasuredEventList
  }
}
