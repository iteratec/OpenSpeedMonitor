import { Component, OnInit, ElementRef, OnChanges} from '@angular/core';
import { ThresholdRestService } from './service/rest/threshold-rest.service';
import { ActualMeasuredEventsService } from './service/actual-measured-events.service';
import {Measurand} from './service/model/measurand.model'
import {MeasuredEvent} from './service/model/measured-event.model'
import {ThresholdForJob} from './service/model/threshold-for-job.model'
import {Threshold} from "./service/model/threshold.model";
import {ActualThresholdsForJobService} from "./service/actual-thresholds-for-job.service";
import {Observable} from "rxjs/index";
import {log} from "util";


@Component({
  selector: 'app-job-threshold',
  templateUrl: './job-threshold.component.html',
  styleUrls: ['./job-threshold.component.css']
})

export class JobThresholdComponent implements OnInit, OnChanges {
  thresholdsForJobList$: Observable<ThresholdForJob[]>;
  thresholdsForJobList: ThresholdForJob[];
  actualMeasuredEventList: MeasuredEvent[];
  measuredEventList: MeasuredEvent[];
  jobId : number;
  scriptId : number;
  newThresholdForJob: ThresholdForJob;
  addMeasuredEventDisabled: boolean = false;

  constructor(private thresholdRestService: ThresholdRestService,
              private actualMeasuredEventsService: ActualMeasuredEventsService,
              private actualThresholdsForJobService: ActualThresholdsForJobService,
              elm: ElementRef) {
    this.jobId = elm.nativeElement.getAttribute('data-job-id');
    this.scriptId = elm.nativeElement.getAttribute('data-job-scriptId');
    this.thresholdRestService.actualJobId = this.jobId;
    this.thresholdRestService.getMeasuredEvents(this.scriptId, this.jobId);
    this.thresholdsForJobList$ = this.actualThresholdsForJobService.actualThresholdsforJobList$;
    this.thresholdRestService.measuredEvents$.subscribe((next: MeasuredEvent[]) => {
      this.measuredEventList = next;
      this.actualMeasuredEventsService.setActualMeasuredEvents(this.measuredEventList);
      this.actualThresholdsForJobService.actualThresholdsforJobList$.subscribe((next: ThresholdForJob[]) => {
        this.thresholdsForJobList = next;
        if(this.thresholdsForJobList.length === this.measuredEventList.length) {
         this.addMeasuredEventDisabled= true;
         }
      });
    });
  }

  ngOnInit() {
    console.log("THRESHOLDFORJOB ngOnInit" );
  }

  ngOnChanges() {
    console.log("THRESHOLDFORJOB ngOnChanges")
  }

  addMeasuredEvent() {
    console.log("THRESHOLDFORJOB addMeasuredEvent")
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

/*
    this.actualMeasuredEventList.length < 1 ? this.addMeasuredEventDisabled = true : this.addMeasuredEventDisabled = false;
*/
  }

  createScript() {
    this.thresholdRestService.getScritpt();
  }

  cancelNewMeasuredEvent() {
    this.thresholdsForJobList.pop();
    this.addMeasuredEventDisabled= false;
  }

  removeOldMeasuredEvent() {
    this.addMeasuredEventDisabled= false;
  }

  addedMeasure() {
    console.log("THRESHOLDFORJOB addedMeasure");
    this.actualMeasuredEventList.length < 1 ? this.addMeasuredEventDisabled = true : this.addMeasuredEventDisabled = false;
  }

}
