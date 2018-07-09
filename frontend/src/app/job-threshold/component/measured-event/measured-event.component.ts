import { Component, OnInit, Input } from '@angular/core';
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
export class MeasuredEventComponent implements OnInit {
  measurandList: Measurand[];
  @Input() measuredEvent: MeasuredEvent;
  @Input() thresholds: Threshold[];
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

  /*ngOnDestroy() {
    this.thresholdRestService.measurands$.unsubscribe();
  }*/

  add() {
    console.log("ADD THRESHOLD");
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

}
