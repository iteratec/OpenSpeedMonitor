import { Component, OnInit, ElementRef} from '@angular/core';
import { ThresholdRestService } from './service/rest/threshold-rest.service';
import {Measurand} from './service/model/measurand.model'
import {MeasuredEvent} from './service/model/measured-event.model'
import {ThresholdForJob} from './service/model/threshold-for-job.model'
import {Threshold} from "./service/model/threshold.model";
import {Observable} from "rxjs";

@Component({
  selector: 'app-job-threshold',
  templateUrl: './job-threshold.component.html',
  styleUrls: ['./job-threshold.component.css']
})

export class JobThresholdComponent implements OnInit {
  //thresholdsForJobList$: Observable<ThresholdForJob[]>;
  thresholdsForJobList: ThresholdForJob[];
  measurandList: Measurand[];

  jobId : number;
  scriptId : number;
  newThresholdForJob: ThresholdForJob;


  constructor(private thresholdRestService: ThresholdRestService,
              elm: ElementRef) {
    this.jobId = elm.nativeElement.getAttribute('data-job-id');
    this.scriptId = elm.nativeElement.getAttribute('data-job-scriptId');
    this.thresholdRestService.actualJobId = this.jobId;
    this.fetchData();

  }

  ngOnInit() {
    //this.thresholdsForJobList.map(m => m.measuredEvent.state = "normal");
  }

  fetchData() {
    //this.thresholdRestService.getMeasurands();
    this.thresholdRestService.measurands$.subscribe((next: Measurand[]) => {
      this.measurandList = next;
    } );
    this.thresholdRestService.getMeasuredEvents(this.scriptId);
    this.thresholdRestService.getThresholdsForJob(this.jobId);
    //this.thresholdsForJobList$ = this.thresholdRestService.thresholdsForJob$;
    this.thresholdRestService.thresholdsForJob$.subscribe((next:ThresholdForJob[]) => {
      this.thresholdsForJobList = next;
      this.thresholdsForJobList.map(element => {
        element.measuredEvent.state = "normal";
        element.thresholds.map(threshold => {
          threshold.state = "normal";
        })
      });

    })
  }

  addMeasuredEvent() {
    console.log("ADD MEASUREDEVENT");
    this.newThresholdForJob = {} as ThresholdForJob;
    let newThreshold = {} as Threshold;
    //let newMeasurand = {} as Measurand;
    let newMeasuredEvent = {} as MeasuredEvent;
    newMeasuredEvent.state = "new";
    //let newThresholdName: string;


    newThreshold.measurand = this.measurandList[0];
    //this.newThreshold.measurand.name = newThresholdName;
    newThreshold.lowerBoundary = 0;
    newThreshold.upperBoundary = 0;
    newThreshold.state = "new";
    newThreshold.measuredEvent = newMeasuredEvent;
    newThreshold.measuredEvent.state = "new";


    this.newThresholdForJob.measuredEvent = newMeasuredEvent;
    this.newThresholdForJob.thresholds = [];
    this.newThresholdForJob.thresholds.push(newThreshold);
    console.log("BEFORE PUSH this.thresholdsForJobList: " + JSON.stringify(this.thresholdsForJobList));
    this.thresholdsForJobList.push(this.newThresholdForJob);
    console.log("this.newThresholdForJob: " + JSON.stringify(this.newThresholdForJob));
    console.log("AFTER PUSH this.thresholdsForJobList: " + JSON.stringify(this.thresholdsForJobList));


  }

  createScript() {
    console.log("createScript");
  }

}
