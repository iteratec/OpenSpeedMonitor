import { Component, OnInit, ElementRef, OnChanges} from '@angular/core';
import { ThresholdRestService } from './service/rest/threshold-rest.service';
import {Measurand} from './service/model/measurand.model'
import {MeasuredEvent} from './service/model/measured-event.model'
import {ThresholdForJob} from './service/model/threshold-for-job.model'
import {Threshold} from "./service/model/threshold.model";

@Component({
  selector: 'app-job-threshold',
  templateUrl: './job-threshold.component.html',
  styleUrls: ['./job-threshold.component.css']
})

export class JobThresholdComponent implements OnInit, OnChanges {
  thresholdsForJobList: ThresholdForJob[];
  measurandList: Measurand[];
  measuredEventList: MeasuredEvent[];
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
  }

  ngOnChanges() {
    this.thresholdsForJobList.map(job => {
      job.thresholds.map(threshold => {
        let name: string = threshold.measuredEvent.name;
        if(this.measuredEventList.map(element => element.name).indexOf(name) !== -1 ) {
          this.measuredEventList.splice(this.measuredEventList.map(element => element.name).indexOf(name) , 1);
        }
      })
    })
  }

  fetchData() {
    this.thresholdRestService.getMeasurands();
    this.thresholdRestService.measurands$.subscribe((next: Measurand[]) => {
      this.measurandList = next;
    } );
    this.thresholdRestService.measuredEvents$.subscribe((next: MeasuredEvent[]) => {
      this.measuredEventList = next;
    } );
    this.thresholdRestService.getMeasuredEvents(this.scriptId);
    this.thresholdRestService.getThresholdsForJob(this.jobId);
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
    this.thresholdsForJobList.map(job => {
      job.thresholds.map(threshold => {
        let name: string = threshold.measuredEvent.name;
        if(this.measuredEventList.map(element => element.name).indexOf(name) !== -1 ) {
          this.measuredEventList.splice(this.measuredEventList.map(element => element.name).indexOf(name) , 1);
        }
      })
    })

    this.newThresholdForJob = {} as ThresholdForJob;
    let newThreshold = {} as Threshold;
    let newMeasuredEvent = {} as MeasuredEvent;
    newMeasuredEvent.state = "new";
    newThreshold.measurand = this.measurandList[0];
    newThreshold.lowerBoundary = 0;
    newThreshold.upperBoundary = 0;
    newThreshold.state = "new";
    newThreshold.measuredEvent = newMeasuredEvent;
    newThreshold.measuredEvent.state = "new";

    this.newThresholdForJob.measuredEvent = newMeasuredEvent;
    this.newThresholdForJob.thresholds = [];
    this.newThresholdForJob.thresholds.push(newThreshold);
    this.thresholdsForJobList.push(this.newThresholdForJob);
  }

  createScript() {
    console.log("createScript");
  }

  removeMeasuredEvent(measuredEvent) {
    this.measuredEventList.push(measuredEvent)
    this.thresholdsForJobList.pop();
  }

}
