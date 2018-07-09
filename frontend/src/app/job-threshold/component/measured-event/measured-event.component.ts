import { Component, OnInit, Input, EventEmitter, Output, OnChanges  } from '@angular/core';
import {MeasuredEvent} from '../../service/model/measured-event.model';
import {Threshold} from '../../service/model/threshold.model';
import {Measurand} from "../../service/model/measurand.model";
import { ThresholdRestService } from '../../service/rest/threshold-rest.service';
import {Observable} from "rxjs";
import {log} from "util";
import {AbstractJsEmitterVisitor} from "@angular/compiler/src/output/abstract_js_emitter";

@Component({
  selector: 'osm-measured-event',
  templateUrl: './measured-event.component.html',
  styleUrls: ['./measured-event.component.css']
})
export class MeasuredEventComponent implements OnInit, OnChanges {
  measurandList: Measurand[];

  @Input() measuredEvent: MeasuredEvent;
  @Input() thresholds: Threshold[];
  @Input() measuredEventList: MeasuredEvent[];
  @Output() removeEvent = new EventEmitter();
  allowthresholdAdd = false;
  newThreshold: Threshold;



  constructor(private thresholdRestService: ThresholdRestService) {
    this.thresholdRestService.measurands$.subscribe((next: Measurand[]) => {
      this.measurandList = next;



    } );

  }

  ngOnInit() {
    //this.thresholds.map(t => t.state = "normal");
    console.log("measured-event-component ngOninit this.measuredEvent: " + JSON.stringify(this.measuredEvent))
    console.log("measured-event-component ngOninit this.thresholds: " + JSON.stringify(this.thresholds));
  }

  ngOnChanges() {
    console.log("BEFORE measuredEventList " + JSON.stringify(this.measuredEventList));

    this.thresholds.map(threshold => {
      let name: string = threshold.measurand.name;
      console.log("INSIDE name: " + JSON.stringify(name));
      console.log("INSIDE indexOf: " + JSON.stringify(this.measurandList.map(element => element.name).indexOf(name)));

      if(this.measurandList.map(element => element.name).indexOf(name) !== -1 ) {
        this.measurandList.splice(this.measurandList.map(element => element.name).indexOf(name) , 1);
        console.log("INSIDE measuredEventList: " + JSON.stringify(this.measurandList));
      }

    })
    console.log(JSON.stringify("AFTER: " + this.measurandList));
  }
  /*ngOnDestroy() {
    this.thresholdRestService.measurands$.unsubscribe();
  }*/

  add() {
    console.log("ADD THRESHOLD");

    console.log("BEFORE measuredEventList " + JSON.stringify(this.measuredEventList));

    this.thresholds.map(threshold => {
      let name: string = threshold.measurand.name;
      console.log("INSIDE name: " + JSON.stringify(name));
      console.log("INSIDE indexOf: " + JSON.stringify(this.measurandList.map(element => element.name).indexOf(name)));

      if(this.measurandList.map(element => element.name).indexOf(name) !== -1 ) {
        this.measurandList.splice(this.measurandList.map(element => element.name).indexOf(name) , 1);
        console.log("INSIDE measuredEventList: " + JSON.stringify(this.measurandList));
      }

    })
    console.log(JSON.stringify("AFTER: " + this.measurandList));



    this.newThreshold = {} as Threshold;
    let newMeasurand = {} as Measurand;
    let newMeasuredEvent = {} as MeasuredEvent;
    let newThresholdName: string;

    if (this.thresholds.length < this.measurandList.length) {
      newThresholdName = this.measurandList[this.thresholds.length].name;
    }
    newMeasuredEvent.id = this.measuredEvent.id;
    //newThreshold.id = 5;
    this.newThreshold.measurand = newMeasurand;
    this.newThreshold.measurand.name = newThresholdName;
    this.newThreshold.lowerBoundary = 0;
    this.newThreshold.upperBoundary = 0;
    this.newThreshold.state = "new";
    this.newThreshold.measuredEvent = newMeasuredEvent;
    console.log("this.newThreshold: " + JSON.stringify(this.newThreshold));

    this.thresholds.push(this.newThreshold);
    this.allowthresholdAdd = !this.allowthresholdAdd;
  }

  removeThreshold() {
    console.log(" remove Threshold ");
      this.thresholds.pop();
  }
  removeMeasuredEvent() {
    console.log(" remove MeasuredEvents ");
    this.removeEvent.emit();
  }





}
