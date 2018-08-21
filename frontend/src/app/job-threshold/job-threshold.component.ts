import {Component, ElementRef} from "@angular/core";
import {ThresholdRestService} from "./services/threshold-rest.service";
import {MeasuredEventService} from "./services/measured-event.service";
import {Measurand} from "./models/measurand.model";
import {MeasuredEvent} from "./models/measured-event.model";
import {ThresholdGroup} from "./models/threshold-for-job.model";
import {Threshold} from "./models/threshold.model";
import {ThresholdService} from "./services/threshold.service";
import {BehaviorSubject, combineLatest, Observable, Subject} from "rxjs";
import {map} from "rxjs/operators";


@Component({
  selector: 'app-job-threshold',
  templateUrl: './job-threshold.component.html',
  styleUrls: ['./job-threshold.component.css']
})

export class JobThresholdComponent {

  jobId: number;
  scriptId: number;

  addMeasuredEventDisabled: boolean = false;
  isEmpty: boolean = false;

  private newThreshold$ = new BehaviorSubject<ThresholdGroup>(null);
  allThresholds$: Observable<ThresholdGroup[]>;

  constructor(private thresholdRestService: ThresholdRestService,
              private measuredEventService: MeasuredEventService,
              private thresholdService: ThresholdService,
              elm: ElementRef) {

    this.jobId = elm.nativeElement.getAttribute('data-job-id');
    this.scriptId = elm.nativeElement.getAttribute('data-job-scriptId');

    this.initialize();
    this.measuredEventService.fetchEvents(this.scriptId, this.jobId);
  }

  private initialize() {

    this.allThresholds$ = combineLatest(
      this.thresholdService.thresholdGroups$,
      this.newThreshold$
    ).pipe(
      map(([measuredEventsWithThresholds, newThresholdForJob]: [ThresholdGroup[], ThresholdGroup]) => {
        return newThresholdForJob ?
          [...measuredEventsWithThresholds, newThresholdForJob] : measuredEventsWithThresholds
      }));

    combineLatest(
      this.measuredEventService.measuredEvents$,
      this.allThresholds$
    ).subscribe(([measuredEvents, measuredEventsWithThresholds]: [MeasuredEvent[], ThresholdGroup[]]) => {
      this.isEmpty = measuredEventsWithThresholds.length == 0;
      if (measuredEventsWithThresholds.length === measuredEvents.length) {
        this.addMeasuredEventDisabled = true;
      }
    });

  }

  addMeasuredEvent() {
    const newThresholdForJob = {} as ThresholdGroup;
    let newThreshold = {} as Threshold;
    let newMeasuredEvent = {} as MeasuredEvent;
    let newMeasurand = {} as Measurand;
    newMeasuredEvent.state = "new";
    newThreshold.measurand = newMeasurand;
    newThreshold.lowerBoundary = 0;
    newThreshold.upperBoundary = 0;
    newThreshold.state = "new";
    newThreshold.measuredEvent = newMeasuredEvent;
    newThreshold.measuredEvent.state = "new";
    newThresholdForJob.measuredEvent = newMeasuredEvent;
    newThresholdForJob.thresholds = [];
    newThresholdForJob.thresholds.push(newThreshold);
    this.newThreshold$.next(newThresholdForJob);
    this.addMeasuredEventDisabled = true;
  }

  createScript() {
    this.thresholdRestService.getScritpt();
  }

  cancelNewMeasuredEvent() {
    this.newThreshold$.next(null);
    this.addMeasuredEventDisabled = false;
  }

  removeOldMeasuredEvent() {
    this.addMeasuredEventDisabled = false;
  }

}
