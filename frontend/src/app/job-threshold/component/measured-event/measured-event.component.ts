import { Component, OnInit, Input, OnDestroy } from '@angular/core';
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
export class MeasuredEventComponent implements OnInit, OnDestroy {
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
    this.thresholds.map(t => t.state = "normal");
    console.log("this.measuredEvent: " + JSON.stringify(this.measuredEvent))
    console.log("ngOninit this.thresholds: " + JSON.stringify(this.thresholds));
  }

  ngOnDestroy() {
    this.thresholdRestService.measurands$.unsubscribe();
  }

  add() {
    console.log("ADD");
    console.log("this.measurandList$: " + JSON.stringify(this.measurandList));
    console.log("this.thresholds: " + JSON.stringify(this.thresholds));
    this.newThreshold = {} as Threshold;
    let newMeasurand = {} as Measurand;
    let newThresholdName: string;

    if (this.thresholds.length < this.measurandList.length) {
      newThresholdName = this.measurandList[this.thresholds.length].name;
    }
    //newThreshold.id = 5;
    this.newThreshold.measurand = newMeasurand;
    this.newThreshold.measurand.name = newThresholdName;
    this.newThreshold.lowerBoundary = 0;
    this.newThreshold.upperBoundary = 0;
    this.newThreshold.state = "new";
    console.log("this.newThreshold: " + JSON.stringify(this.newThreshold));

    this.thresholds.push(this.newThreshold);
    this.allowthresholdAdd = !this.allowthresholdAdd;


    //Which is the next measurand to be used?
    /*thresholds.forEach(function (threshold) {

    } )*/

    /*//newThreshold.measurand = ;
    newThreshold.measuredEvent = measuredEvent.id;
    newThreshold.lowerBoundary = 0;
    newThreshold.upperBoundary = 0;
    this.allowthreshold = !this.allowthreshold;
    threshold.id =
    this.allowthreshold
      ? console.log("this.allowthreshold true: " + this.allowthreshold)
      : console.log("this.allowthreshold false: " + this.allowthreshold)
    ;*/
  }

}
