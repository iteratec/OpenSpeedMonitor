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

  private newThresholGroup$ = new BehaviorSubject<ThresholdGroup>(null);
  allThresholdGroups$: Observable<ThresholdGroup[]>;
  unusedMeasuredEvents$: Observable<MeasuredEvent[]>;

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

    this.allThresholdGroups$ = combineLatest(
      this.thresholdService.thresholdGroups$,
      this.newThresholGroup$
    ).pipe(
      map(([thresholdGroups, newThresholdGroup]: [ThresholdGroup[], ThresholdGroup]) => {
        return newThresholdGroup ?
          [...thresholdGroups, newThresholdGroup] : thresholdGroups
      }));

    combineLatest(
      this.measuredEventService.measuredEvents$,
      this.allThresholdGroups$
    ).subscribe(([measuredEvents, thresholdGroups]: [MeasuredEvent[], ThresholdGroup[]]) => {
      this.isEmpty = thresholdGroups.length == 0;
      if (thresholdGroups.length === measuredEvents.length) {
        this.addMeasuredEventDisabled = true;
      }
    });

    this.unusedMeasuredEvents$ = combineLatest(
      this.thresholdService.thresholdGroups$,
      this.measuredEventService.measuredEvents$
    ).pipe(
      map(([thresholdGroups, measuredEvents]: [ThresholdGroup[], MeasuredEvent[]]) => {
        return measuredEvents.filter((measuredEvent: MeasuredEvent) =>
          !thresholdGroups.some(thresholdGroup => thresholdGroup.measuredEvent.id == measuredEvent.id)
        )
      })
    )
  }

  addThresholdGroup() {
    const newThresholdGroup = {} as ThresholdGroup;
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
    newThresholdGroup.measuredEvent = newMeasuredEvent;
    newThresholdGroup.thresholds = [];
    newThresholdGroup.thresholds.push(newThreshold);
    this.newThresholGroup$.next(newThresholdGroup);
    this.addMeasuredEventDisabled = true;
  }

  createScript() {
    this.thresholdRestService.getScritpt();
  }

  cancelNewMeasuredEvent() {
    this.newThresholGroup$.next(null);
    this.addMeasuredEventDisabled = false;
  }

  removeOldMeasuredEvent() {
    this.addMeasuredEventDisabled = false;
  }

}
