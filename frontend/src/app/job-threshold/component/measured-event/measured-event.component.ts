import { Component, OnInit, Input, EventEmitter, Output, OnChanges  } from '@angular/core';
import {MeasuredEvent} from '../../service/model/measured-event.model';
import {Threshold} from '../../service/model/threshold.model';
import {Measurand} from "../../service/model/measurand.model";
import { ThresholdRestService } from '../../service/rest/threshold-rest.service';
import { ActualMeasurandsService } from '../../service/actual-measurands.service';

@Component({
  selector: 'osm-measured-event',
  templateUrl: './measured-event.component.html',
  styleUrls: ['./measured-event.component.css']
})

export class MeasuredEventComponent implements OnInit, OnChanges {
  @Input() measuredEvent: MeasuredEvent;
  @Input() thresholds: Threshold[];
  @Input() measuredEventList: MeasuredEvent[];
  @Output() removeEvent = new EventEmitter();
  newThreshold: Threshold;
  //measurandList: Measurand[];
  actualMeasurands: Measurand[];


  constructor(private thresholdRestService: ThresholdRestService,
              private actualMeasurandsService: ActualMeasurandsService) {
    /*this.thresholdRestService.measurands$.subscribe((next: Measurand[]) => {
     this.measurandList = next;
     } );*/

  }

  ngOnInit() {
    if (this.measuredEvent.state == "new") {
      this.actualMeasurands = this.actualMeasurandsService.getActualMeasurands(this.thresholds);
    }
  }

  ngOnChanges() {
/*    this.thresholds.map(threshold => {
      let name: string = threshold.measurand.name;
      if(this.measurandList.map(element => element.name).indexOf(name) !== -1 ) {
        this.measurandList.splice(this.measurandList.map(element => element.name).indexOf(name) , 1);
      }
    })*/
  }

  addThreshold() {
    this.actualMeasurands = this.actualMeasurandsService.getActualMeasurands(this.thresholds);
    console.log("MEASUREDEVENT this.actualMeasurands: " + JSON.stringify(this.actualMeasurands));
   /* this.thresholds.map(threshold => {
      let name: string = threshold.measurand.name;
      if(this.measurandList.map(element => element.name).indexOf(name) !== -1 ) {
        this.measurandList.splice(this.measurandList.map(element => element.name).indexOf(name) , 1);
      }
    })*/

    this.newThreshold = {} as Threshold;
    let newMeasurand = {} as Measurand;
    let newMeasuredEvent = {} as MeasuredEvent;
    let newThresholdName: string;

    //if (this.thresholds.length < this.actualMeasurands.length) {
      newThresholdName = this.actualMeasurands[this.thresholds.length].name;
    //}
    newMeasuredEvent.id = this.measuredEvent.id;
    this.newThreshold.measurand = newMeasurand;
    this.newThreshold.measurand.name = newThresholdName;
    this.newThreshold.lowerBoundary = 0;
    this.newThreshold.upperBoundary = 0;
    this.newThreshold.state = "new";
    this.newThreshold.measuredEvent = newMeasuredEvent;
    this.thresholds.push(this.newThreshold);
  }

  removeThreshold(threshold) {
    console.log("MEASUREDEVENT removeThreshold");
    console.log(" removeThreshold this.thresholds.length: " + this.thresholds.length);
    if (this.thresholds.length === 1) {
      this.removeEvent.emit(threshold.measuredEvent);
    } else {
      this.thresholds.pop();
    }

  }
  removeMeasuredEvent(threshold) {
    console.log("MEASUREDEVENT removeMeasuredEvent");
    console.log(" removeMeasuredEvent this.thresholds.length: " + this.thresholds.length);
    this.removeEvent.emit(threshold.measuredEvent);
  }
}
