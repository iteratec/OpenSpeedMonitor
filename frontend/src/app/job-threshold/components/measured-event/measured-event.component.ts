import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {MeasuredEvent} from "../../models/measured-event.model";
import {Threshold} from "../../models/threshold.model";
import {Measurand} from "../../models/measurand.model";
import {MeasurandService} from "../../services/measurand.service";

@Component({
  selector: 'osm-measured-event',
  templateUrl: './measured-event.component.html',
  styleUrls: ['./measured-event.component.css']
})

export class MeasuredEventComponent implements OnInit {
  @Input() measuredEvent: MeasuredEvent;
  @Input() thresholds: Threshold[] = [];
  @Output() removeEvent = new EventEmitter();
  @Output() removeOldMeasuredEvent = new EventEmitter();
  newThreshold: Threshold;
  addThresholdDisabled: boolean = false;
  actualMeasurandList: Measurand[];


  constructor(private measurandsService: MeasurandService) {
  }

  ngOnInit() {
    if (this.thresholds) {
      this.actualMeasurandList = this.measurandsService.getActualMeasurands(this.thresholds);
      this.addThresholdDisabled = this.actualMeasurandList.length < 1;
    }

  }

  addThreshold() {
    if (!this.thresholds) {
      return;
    }
    this.addThresholdDisabled = true;
    this.actualMeasurandList = this.measurandsService.getActualMeasurands(this.thresholds);

    this.newThreshold = {
      measurand:  this.actualMeasurandList[0],
      lowerBoundary:  0,
      upperBoundary:  0,
      state: "new",
      measuredEvent: this.measuredEvent
    };

  }

  addedThreshold() {
    this.actualMeasurandList.length < 2 ? this.addThresholdDisabled = true : this.addThresholdDisabled = false;
  }

  removeThreshold() {
    if (this.thresholds.length == 1) {
      this.removeOldMeasuredEvent.emit();
    }
    this.addThresholdDisabled = false;
  }

  cancelNewThreshold() {
    this.thresholds.pop();
    this.addThresholdDisabled = false;

  }

  cancelNewMeasuredEvent() {
    this.removeEvent.emit();
  }
}
