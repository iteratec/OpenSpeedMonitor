import {Injectable} from "@angular/core";
import {MeasuredEvent} from "../models/measured-event.model";
import {ThresholdGroup} from "../models/threshold-for-job.model";
import {ThresholdRestService} from "./threshold-rest.service";
import {ReplaySubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class MeasuredEventService {

  measuredEventList: MeasuredEvent[];
  actualMeasuredEventList: MeasuredEvent[];
  public measuredEvents$ = new ReplaySubject<MeasuredEvent[]>(1);

  constructor(private thresholdRestService: ThresholdRestService) {

  }

  fetchEvents(scriptId, jobId) {
    this.thresholdRestService.actualJobId = jobId;
    this.thresholdRestService.getMeasuredEvents(scriptId, jobId).subscribe(this.measuredEvents$);

    this.measuredEvents$.subscribe((next: MeasuredEvent[]) => {
      this.setActualMeasuredEvents(this.measuredEventList);
    });

  }

  setActualMeasuredEvents(measuredEvents: MeasuredEvent[]) {
    this.measuredEventList = measuredEvents;
  }

  getActualMeasuredEvents(thresholdsforJob: ThresholdGroup[]) {
    this.actualMeasuredEventList = [];
    this.measuredEventList.map(measuredEvent => {
      if (thresholdsforJob.map(thresholdForJob => thresholdForJob.measuredEvent.name).indexOf(measuredEvent.name) == -1) {
        this.actualMeasuredEventList.push(measuredEvent);
      }
    });
    return this.actualMeasuredEventList
  }
}
