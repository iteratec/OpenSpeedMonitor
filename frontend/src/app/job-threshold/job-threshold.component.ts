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

/*var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.i18n = OpenSpeedMonitor.i18n || {};
OpenSpeedMonitor.i18n.measurands = OpenSpeedMonitor.i18n.measurands || {};*/

export class JobThresholdComponent implements OnInit, OnChanges {


  private activeMeasuredEvents: ThresholdForJob;
  private jobId : number;
  private scriptId : number;
  private measurands : Measurand[];
  private measuredEvents : MeasuredEvent[];
  private copiedMeasuredEvents: MeasuredEvent[];
  private measuredEventCount : number;
  private thresholdsForEvent :  Threshold[];
  private measured : MeasuredEvent;


  constructor(private thresholdRestService: ThresholdRestService,
              elm: ElementRef) {
    this.jobId = elm.nativeElement.getAttribute('data-job-id');
    this.scriptId = elm.nativeElement.getAttribute('data-job-scriptId');
  }

  ngOnChanges() {
    /*this.jobId = this.$el.attributes['jobId'].value;*/
    /*this.scriptId = this.$el.attributes['scriptId'].value;*/
  }

  ngOnInit() {


    /*console.log("ngOnInit measurands: " + this.measurands);
    console.log("ngOnInit jobId: " + this.jobId);
    console.log("ngOnInit scriptId: " + this.scriptId);*/

    this.fetchData();
  }

  fetchData() {
    this.thresholdRestService.getMeasurands().subscribe((measurands: Measurand[]) => {
      console.log("measurands: " + JSON.stringify(measurands ));
      /*measurands.forEach(function (measurand) {
        measurand.translatedName = this.OpenSpeedMonitor.i18n.measurands[measurand.name];
      });*/
      /*console.log("after measurands: " + JSON.stringify(measurands ));*/

    });

    this.thresholdRestService.getMeasuredEvents(this.scriptId).subscribe((measuredEvents: MeasuredEvent[]) => {
/*
      console.log("after measuredEvents: " + JSON.stringify(measuredEvents ));
*/
      this.measuredEvents = measuredEvents;
      this.copiedMeasuredEvents = this.measuredEvents.slice();   //useless Guacamole
      this.measuredEventCount = this.measuredEvents.length;
      /*console.log("this.measuredEvents: " + JSON.stringify(this.measuredEvents ));
      console.log("this.copiedMeasuredEvents: " + JSON.stringify(this.copiedMeasuredEvents ));
      console.log("this.measuredEventCount: " + JSON.stringify(this.measuredEventCount ));*/

      this.activeMeasuredEvents = this.getThresholds();

    });
  }

  getThresholds() {
    //this.activeMeasuredEvents = [];
    let activeMeasuredEventsTemp = {} as ThresholdForJob;
    let self = this;
    this.thresholdRestService.getThresholdsForJob(this.jobId).subscribe((response: ThresholdForJob[]) => {
      response.forEach(function (responseEvent) {
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
          thresholdsForEvent.push(thresholdEdited)
        });
         let measured : MeasuredEvent = self.measuredEvents.find(function (element) {
          return element.id === responseEvent.measuredEvent.id;
        });

        console.log(" measured: " + JSON.stringify(measured ));
        activeMeasuredEventsTemp.measuredEvent = measured;
        activeMeasuredEventsTemp.thresholds = thresholdsForEvent;
      });
      console.log(" self.activeMeasuredEvents: " + JSON.stringify(activeMeasuredEventsTemp ));

    });
    return activeMeasuredEventsTemp;
  }

}
