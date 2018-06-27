import { Component, OnInit, ElementRef, OnChanges} from '@angular/core';
import { ThresholdRestService } from './service/rest/threshold-rest.service';

@Component({
  selector: 'app-job-threshold',
  templateUrl: './job-threshold.component.html',
  styleUrls: ['./job-threshold.component.css']
})

/*var OpenSpeedMonitor = OpenSpeedMonitor || {};
OpenSpeedMonitor.i18n = OpenSpeedMonitor.i18n || {};
OpenSpeedMonitor.i18n.measurands = OpenSpeedMonitor.i18n.measurands || {};*/

export class JobThresholdComponent implements OnInit, OnChanges {


  private activeMeasuredEvents: any[];
  private jobId : number;
  private scriptId : number;
  private measurands : any[];
  private measuredEvents : any[];
  private copiedMeasuredEvents: any[];
  private measuredEventCount : number;


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
    this.thresholdRestService.getMeasurands().subscribe((measurands: any[]) => {
      console.log("measurands: " + JSON.stringify(measurands ));
      /*measurands.forEach(function (measurand) {
        measurand.translatedName = this.OpenSpeedMonitor.i18n.measurands[measurand.name];
      });*/
      /*console.log("after measurands: " + JSON.stringify(measurands ));*/

    });

    this.thresholdRestService.getMeasuredEvents(this.scriptId).subscribe((measuredEvents: any[]) => {
/*
      console.log("after measuredEvents: " + JSON.stringify(measuredEvents ));
*/
      this.measuredEvents = measuredEvents;
      this.copiedMeasuredEvents = this.measuredEvents.slice();   //useless Guacamole
      this.measuredEventCount = this.measuredEvents.length;
      /*console.log("this.measuredEvents: " + JSON.stringify(this.measuredEvents ));
      console.log("this.copiedMeasuredEvents: " + JSON.stringify(this.copiedMeasuredEvents ));
      console.log("this.measuredEventCount: " + JSON.stringify(this.measuredEventCount ));*/

      this.getThresholds();

    });
  }

  getThresholds() {
    this.activeMeasuredEvents = [];
    var self = this;
    this.thresholdRestService.getThresholdsForJob(this.jobId).subscribe((result: any[]) => {
    /*console.log("getThresholdsForJob result: " + JSON.stringify(result ));*/
      result.forEach(function (resultEvent) {
        var thresholdsForEvent = [];
        resultEvent.thresholds.forEach(function (threshold) {
          thresholdsForEvent.push({
            threshold: threshold,
            edit: false,
            saved: true
          })
        });
        self.activeMeasuredEvents.push({
          measuredEvent: self.measuredEvents.find(function (element) {
            return element.id === resultEvent.measuredEvent.id;
          }),
          thresholdList: thresholdsForEvent
        })
      });
      console.log(" self.activeMeasuredEvents: " + JSON.stringify(self.activeMeasuredEvents ));

    });
  }

}
