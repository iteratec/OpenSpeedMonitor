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
  @Output() addedMeasure = new EventEmitter();
  @Output() removeEvent = new EventEmitter();
  @Output() removeOldMeasuredEvent = new EventEmitter();
  newThreshold: Threshold;
  addThresholdDisabled: boolean = false;
  actualMeasurandList: Measurand[];


  constructor(private thresholdRestService: ThresholdRestService,
              private actualMeasurandsService: ActualMeasurandsService,
  ) {}

  ngOnInit() {
    console.log("MEASUREDEVENT");
    console.log("MEASUREDEVENT this.measuredEvent.state: " + this.measuredEvent.state);
    this.actualMeasurandList = this.actualMeasurandsService.getActualMeasurands(this.thresholds);
    this.actualMeasurandList.length < 1 ? this.addThresholdDisabled = true : this.addThresholdDisabled = false;
  }

  ngOnChanges() {
    console.log("MEASUREDEVENT ngOnChanges");
  }

  addThreshold() {
    this.addThresholdDisabled = true;
    this.actualMeasurandList = this.actualMeasurandsService.getActualMeasurands(this.thresholds);
    this.newThreshold = {} as Threshold;
    let newMeasurand = {} as Measurand;
    let newMeasuredEvent = {} as MeasuredEvent;
    let newThresholdName: string;
    newThresholdName = this.actualMeasurandList[0].name;
    newMeasuredEvent = this.measuredEvent;

    if (this.measuredEvent.state == "new"){
      newMeasuredEvent.state='new';
    } else {
      newMeasuredEvent.state='normal'
    }
    this.newThreshold.measurand = newMeasurand;
    this.newThreshold.measurand.name = newThresholdName;
    this.newThreshold.lowerBoundary = 0;
    this.newThreshold.upperBoundary = 0;
    this.newThreshold.state = "new";
    this.newThreshold.measuredEvent = newMeasuredEvent;
    this.thresholds.push(this.newThreshold);
  }

  /*savedThreshold() {
    console.log("MEASUREDEVENT savedThreshold")
    this.actualMeasurandList.length < 1 ? this.addThresholdDisabled = true : this.addThresholdDisabled = false;
  }*/

  removeThreshold() {
    if (this.thresholds.length == 1) {
      this.removeOldMeasuredEvent.emit();
    }
    this.addThresholdDisabled = false;
  }

  addedMeasuredEvent() {
    console.log("MEASUREDEVENT addedMeasuredEvent");
    this.addedMeasure.emit()
  }
  addedThreshold(){
    console.log("MEASUREDEVENT addedThreshold this.ahctualMeasurandList.length: " + this.actualMeasurandList.length);
    this.actualMeasurandList.length < 2 ? this.addThresholdDisabled = true : this.addThresholdDisabled = false;
    //this.actualMeasuredEventList.length < 1 ? this.addMeasuredEventDisabled = true : this.addMeasuredEventDisabled = false;


  }

  cancelNewThreshold(){
    this.thresholds.pop();
    this.addThresholdDisabled = false;

  }

  cancelNewMeasuredEvent() {
    this.removeEvent.emit();
  }
}
