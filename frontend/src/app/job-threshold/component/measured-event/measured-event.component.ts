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

export class MeasuredEventComponent implements OnInit {
  @Input() measuredEvent: MeasuredEvent;
  @Input() thresholds: Threshold[];
  @Input() measuredEventList: MeasuredEvent[];
  @Output() removeEvent = new EventEmitter();
  @Output() removeOldMeasuredEvent = new EventEmitter();
  newThreshold: Threshold;
  addThresholdDisabled: boolean = false;
  actualMeasurandList: Measurand[];


  constructor(private thresholdRestService: ThresholdRestService,
              private actualMeasurandsService: ActualMeasurandsService) {
    /*this.thresholdRestService.measurands$.subscribe((next: Measurand[]) => {
     this.measurandList = next;
     } );*/

  }

  ngOnInit() {

    //if (this.measuredEvent.state == "new") {
    this.actualMeasurandList = this.actualMeasurandsService.getActualMeasurands(this.thresholds);
    //}
    this.actualMeasurandList.length < 1 ? this.addThresholdDisabled = true : this.addThresholdDisabled = false;
    console.log("this.actualMeasurandList.length: " + this.actualMeasurandList.length);


  }

  addThreshold() {
    this.addThresholdDisabled = true;
    this.actualMeasurandList = this.actualMeasurandsService.getActualMeasurands(this.thresholds);
    console.log("MEASUREDEVENT this.actualMeasurandList: " + JSON.stringify(this.actualMeasurandList));
    this.newThreshold = {} as Threshold;
    let newMeasurand = {} as Measurand;
    let newMeasuredEvent = {} as MeasuredEvent;
    let newThresholdName: string;
    newThresholdName = this.actualMeasurandList[0].name;
    newMeasuredEvent.id = this.measuredEvent.id;
    this.newThreshold.measurand = newMeasurand;
    this.newThreshold.measurand.name = newThresholdName;
    this.newThreshold.lowerBoundary = 0;
    this.newThreshold.upperBoundary = 0;
    this.newThreshold.state = "new";
    this.newThreshold.measuredEvent = newMeasuredEvent;
    this.thresholds.push(this.newThreshold);
  }

  savedThreshold() {
    this.actualMeasurandList.length < 1 ? this.addThresholdDisabled = true : this.addThresholdDisabled = false;
  }

  removeThreshold() {
    if (this.thresholds.length == 1) {
      this.removeOldMeasuredEvent.emit();
    }
    this.addThresholdDisabled = false;
  }

  cancelNewThreshold(){
    console.log("MEASUREDEVENT cancelNewThreshold");
    this.thresholds.pop();
    this.addThresholdDisabled = false;

  }

  cancelNewMeasuredEvent() {
    console.log("MEASUREDEVENT cancelNewMeasuredEvent");
    this.removeEvent.emit();
  }
}
