import { Component, OnInit, ElementRef} from '@angular/core';
import { ThresholdRestService } from './service/rest/threshold-rest.service';
import { ActualMeasuredEventsService } from './service/actual-measured-events.service';
import {Measurand} from './service/model/measurand.model'
import {MeasuredEvent} from './service/model/measured-event.model'
import {ThresholdForJob} from './service/model/threshold-for-job.model'
import {Threshold} from "./service/model/threshold.model";

@Component({
  selector: 'app-job-threshold',
  templateUrl: './job-threshold.component.html',
  styleUrls: ['./job-threshold.component.css']
})

export class JobThresholdComponent implements OnInit {
  thresholdsForJobList: ThresholdForJob[];
  //measurandList: Measurand[];
  actualMeasuredEventList: MeasuredEvent[];
  measuredEventList: MeasuredEvent[];
  jobId : number;
  scriptId : number;
  newThresholdForJob: ThresholdForJob;
  addMeasuredEventDisabled: boolean = false;

  constructor(private thresholdRestService: ThresholdRestService,
              private actualMeasuredEventsService: ActualMeasuredEventsService,
              elm: ElementRef) {
    this.jobId = elm.nativeElement.getAttribute('data-job-id');
    this.scriptId = elm.nativeElement.getAttribute('data-job-scriptId');
    this.thresholdRestService.actualJobId = this.jobId;
    this.fetchData();
  }

  ngOnInit() {
    /*if(this.thresholdsForJobList.length === this.measuredEventList.length) {
     this.addMeasuredEventDisabled= true;
    }*/
  }

/*  ngOnChanges() {

    this.thresholdsForJobList.map(job => {
      job.thresholds.map(threshold => {
        let name: string = threshold.measuredEvent.name;
        if(this.measuredEventList.map(element => element.name).indexOf(name) !== -1 ) {
          this.measuredEventList.splice(this.measuredEventList.map(element => element.name).indexOf(name) , 1);
        }
      })
    })
  }*/

  fetchData() {

    //this.thresholdRestService.getMeasurands();
/*    this.thresholdRestService.measurands$.subscribe((next: Measurand[]) => {
      this.measurandList = next;
    } );*/
    this.thresholdRestService.getMeasuredEvents(this.scriptId);

    let self = this;

    this.thresholdRestService.getThresholdsForJob(this.jobId);
    this.thresholdRestService.thresholdsForJob$.subscribe((next:ThresholdForJob[]) => {
      this.thresholdsForJobList = next;
      this.thresholdsForJobList.map(element => {
        element.measuredEvent.state = "normal";
        element.thresholds.map(threshold => {
          threshold.state = "normal";
        })
      });
      self.thresholdRestService.measuredEvents$.subscribe((next: MeasuredEvent[]) => {
        self.measuredEventList = next;
        self.actualMeasuredEventsService.setActualMeasuredEvents(self.measuredEventList);
        if(self.thresholdsForJobList.length === self.measuredEventList.length) {
          self.addMeasuredEventDisabled= true;
        }
      });
    })
  }

  addMeasuredEvent() {
    this.actualMeasuredEventList = this.actualMeasuredEventsService.getActualMeasuredEvents(this.thresholdsForJobList);
    /*this.thresholdsForJobList.map(job => {
      job.thresholds.map(threshold => {
        let name: string = threshold.measuredEvent.name;
        if(this.measuredEventList.map(element => element.name).indexOf(name) !== -1 ) {
          this.measuredEventList.splice(this.measuredEventList.map(element => element.name).indexOf(name) , 1);
        }
      })
    })*/

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
    if(this.thresholdsForJobList.length == this.actualMeasuredEventList.length ) {
      this.addMeasuredEventDisabled= true;
    }
  }



  createScript() {
    console.log("createScript");
  }

  cancelNewMeasuredEvent() {
    console.log("JOB removeMeasuredEvent");
    this.thresholdsForJobList.pop();
    this.addMeasuredEventDisabled= false;
  }

  removeOldMeasuredEvent() {
    this.addMeasuredEventDisabled= false;
  }

}
