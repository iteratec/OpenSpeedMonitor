import {Component, ElementRef, OnChanges, OnInit} from "@angular/core";
import {ThresholdRestService} from "./services/threshold-rest.service";
import {ActualMeasuredEventsService} from "./services/actual-measured-events.service";
import {Measurand} from "./models/measurand.model";
import {MeasuredEvent} from "./models/measured-event.model";
import {ThresholdForJob} from "./models/threshold-for-job.model";
import {Threshold} from "./models/threshold.model";
import {ActualThresholdsForJobService} from "./services/actual-thresholds-for-job.service";


@Component({
  selector: 'app-job-threshold',
  templateUrl: './job-threshold.component.html',
  styleUrls: ['./job-threshold.component.css']
})

export class JobThresholdComponent implements OnInit, OnChanges {
  thresholdsForJobList: ThresholdForJob[];
  actualMeasuredEventList: MeasuredEvent[];
  measuredEventList: MeasuredEvent[];
  jobId : number;
  scriptId : number;
  newThresholdForJob: ThresholdForJob;
  addMeasuredEventDisabled: boolean = false;
  isEmpty: boolean = false;

  constructor(private thresholdRestService: ThresholdRestService,
              private actualMeasuredEventsService: ActualMeasuredEventsService,
              private actualThresholdsForJobService: ActualThresholdsForJobService,
              elm: ElementRef) {
    this.jobId = elm.nativeElement.getAttribute('data-job-id');
    this.scriptId = elm.nativeElement.getAttribute('data-job-scriptId');
    this.thresholdRestService.actualJobId = this.jobId;
    this.thresholdRestService.getMeasuredEvents(this.scriptId, this.jobId);
    this.thresholdRestService.measuredEvents$.subscribe((next: MeasuredEvent[]) => {
      this.measuredEventList = next;
      this.actualMeasuredEventsService.setActualMeasuredEvents(this.measuredEventList);
      this.actualThresholdsForJobService.actualThresholdsforJobList$.subscribe((next: ThresholdForJob[]) => {
        this.thresholdsForJobList = next;
        console.log("this.thresholdsForJobList: " + JSON.stringify(this.thresholdsForJobList));
        if (this.thresholdsForJobList.length == 0) {
          this.isEmpty = true;
        }
        if(this.thresholdsForJobList.length === this.measuredEventList.length) {
         this.addMeasuredEventDisabled= true;
         }
      });
    });
  }

  ngOnInit() {}

  ngOnChanges() {}

  addMeasuredEvent() {
    this.actualMeasuredEventList = this.actualMeasuredEventsService.getActualMeasuredEvents(this.thresholdsForJobList);
    this.newThresholdForJob = {} as ThresholdForJob;
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
    this.newThresholdForJob.measuredEvent = newMeasuredEvent;
    this.newThresholdForJob.thresholds = [];
    this.newThresholdForJob.thresholds.push(newThreshold);
    this.thresholdsForJobList.push(this.newThresholdForJob);
    this.addMeasuredEventDisabled = true;
    this.isEmpty = false;
  }

  createScript() {
    this.thresholdRestService.getScritpt();
  }

  cancelNewMeasuredEvent() {
    this.thresholdsForJobList.pop();
    this.addMeasuredEventDisabled= false;
    this.thresholdsForJobList.length > 0? this.isEmpty = false :this.isEmpty = true ;
  }

  removeOldMeasuredEvent() {
    this.addMeasuredEventDisabled= false;
    this.thresholdsForJobList.length > 1? this.isEmpty = false :this.isEmpty = true ;
  }

  addedMeasure() {
    this.actualMeasuredEventList.length < 1 ? this.addMeasuredEventDisabled = true : this.addMeasuredEventDisabled = false;
  }

}
