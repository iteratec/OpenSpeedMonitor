import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {Threshold} from "../../models/threshold.model";
import {ThresholdRestService} from "../../services/threshold-rest.service";
import {Measurand} from "../../models/measurand.model";
import {MeasuredEvent} from "../../models/measured-event.model";
import {MeasuredEventService} from "../../services/measured-event.service";

@Component({
  selector: 'osm-threshold',
  templateUrl: './threshold.component.html',
  styleUrls: ['./threshold.component.css']
})

export class ThresholdComponent implements OnInit {
  @Input() threshold: Threshold;
  @Input() actualMeasurandList: Measurand [];
  @Output() cancelEvent = new EventEmitter();
  @Output() addedqqThreshold = new EventEmitter();
  @Output() removeOldThreshold = new EventEmitter();
  selectedMeasuredEvent: MeasuredEvent;
  selectedMeasurand: string;

  constructor(private thresholdRestService: ThresholdRestService, private actualMeasuredEventsService: MeasuredEventService) {
  }

  ngOnInit() {

    if (this.threshold) {
      if (this.threshold.measuredEvent.state == "new") {
        this.selectedMeasuredEvent = this.actualMeasuredEventsService.measuredEventList[0];
      }
      if (this.threshold.state == "new") {
        this.selectedMeasurand = this.actualMeasurandList[0].translationsKey;
      }
    } else {
      return undefined;
    }
  }

  edit(obj) {
    this.threshold.lowerBoundary = obj.lowerBoundary;
    this.threshold.upperBoundary = obj.upperBoundary;
    this.thresholdRestService.editThreshold(this.threshold)
  }

  delete() {
    this.thresholdRestService.deleteThreshold(this.threshold);
    this.removeOldThreshold.emit();
  }

  saveNew(obj) {
    let tempArray = this.selectedMeasurand.split('.');
    let selected = tempArray[tempArray.length - 1]; // selected = the selected measurands name instead of measurands translationkey
    this.threshold.measurand.name = selected;
    this.threshold.measurand.translationsKey = this.selectedMeasurand;
    this.threshold.lowerBoundary = obj.lowerBoundary;
    this.threshold.upperBoundary = obj.upperBoundary;
    if (this.threshold.measuredEvent.state == "new") {
      this.threshold.measuredEvent = this.selectedMeasuredEvent;
      this.threshold.measuredEvent.state = "new";
      this.thresholdRestService.addThreshold(this.threshold);
    }
    else {
      this.thresholdRestService.addThreshold(this.threshold);
      this.addedThreshold.emit();
    }
  }

  cancelNew() {
    this.cancelEvent.emit();
  }
}
