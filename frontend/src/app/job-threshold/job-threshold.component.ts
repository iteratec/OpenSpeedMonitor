import { Component, OnInit, ElementRef} from '@angular/core';
import { ThresholdRestService } from './service/rest/threshold-rest.service';
import {Measurand} from './service/model/measurand.model'
import {MeasuredEvent} from './service/model/measured-event.model'
import {ThresholdForJob} from './service/model/threshold-for-job.model'
import {Threshold} from "./service/model/threshold.model";
import {ObservableInput} from "rxjs/index";
import {Observable} from "rxjs";

@Component({
  selector: 'app-job-threshold',
  templateUrl: './job-threshold.component.html',
  styleUrls: ['./job-threshold.component.css']
})

export class JobThresholdComponent implements OnInit {

  public activeMeasuredEvents: ThresholdForJob [];
  private jobId : number;
  private scriptId : number;
  private measurands : Measurand[];
  private measuredEvents : MeasuredEvent[];
  private copiedMeasuredEvents: MeasuredEvent[];
  private measuredEventCount : number;
  private measured : MeasuredEvent;

  activeMeasuredEvents$: Observable<ThresholdForJob>;

  constructor(private thresholdRestService: ThresholdRestService,
              elm: ElementRef) {
    this.jobId = elm.nativeElement.getAttribute('data-job-id');
    this.scriptId = elm.nativeElement.getAttribute('data-job-scriptId');
  }

  ngOnInit() {
    this.fetchData();
  }

  fetchData() {
    this.thresholdRestService.getMeasurands().subscribe((measurands: Measurand[]) => {
      this.measurands = measurands;
      console.log("measurands: " + JSON.stringify(this.measurands ));
    });

    this.thresholdRestService.getMeasuredEvents(this.scriptId).subscribe((measuredEvents: MeasuredEvent[]) => {
      this.measuredEvents = measuredEvents;
      this.copiedMeasuredEvents = this.measuredEvents.slice();   //useless Guacamole
      this.measuredEventCount = this.measuredEvents.length;
      /*let self = this;
      this.activeMeasuredEvents = this.getThresholds();*/
      this.thresholdRestService.getThresholdsForJob(this.jobId).subscribe((response: ThresholdForJob[]) => {
        this.activeMeasuredEvents = response;
        console.log(" activeMeasuredEvents 1: " + JSON.stringify(this.activeMeasuredEvents));
      })
      console.log(" activeMeasuredEvents 2: " + JSON.stringify(this.activeMeasuredEvents));

    });
  }

  /*getThresholds() {
    /!*let activeMeasuredEventTemp = {} as ThresholdForJob;*!/
    let activeMeasuredEventsTemp: ThresholdForJob[] = [];

    this.thresholdRestService.getThresholdsForJob(this.jobId).subscribe((response: ThresholdForJob[]) => {
      /!*response.forEach(function (responseEvent) {
        let thresholdsForEvent: Threshold[] = [];
        let thresholdEdited = {} as Threshold;
        responseEvent.thresholds.forEach(function (threshold) {
          thresholdEdited.id = threshold.id ;
          thresholdEdited.lowerBoundary = threshold.lowerBoundary ;
          thresholdEdited.measurand = threshold.measurand ;
          thresholdEdited.measuredEvent = threshold.measuredEvent ;
          thresholdEdited.upperBoundary = threshold.upperBoundary ;
          thresholdEdited.edit = false;
          thresholdEdited.saved = true;
          console.log("getThresholds forEach forEach thresholdEdited: " + JSON.stringify(thresholdEdited ));
          thresholdsForEvent.push(thresholdEdited);
        });
         let measured : MeasuredEvent = self.measuredEvents.find(function (element) {
          return element.id === responseEvent.measuredEvent.id;
        });
        console.log(" measured: " + JSON.stringify(measured));
        activeMeasuredEventTemp.measuredEvent = measured;
        activeMeasuredEventTemp.thresholds = thresholdsForEvent;
        activeMeasuredEventsTemp.push(activeMeasuredEventTemp);
      });*!/
      activeMeasuredEventsTemp = response;
      console.log(" activeMeasuredEventsTemp 1: " + JSON.stringify(activeMeasuredEventsTemp));
    });
    console.log(" activeMeasuredEventsTemp 2: " + JSON.stringify(activeMeasuredEventsTemp));
    return activeMeasuredEventsTemp;
  }*/

}
