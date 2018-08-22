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

export class MeasuredEventComponent {
  private _thresholds: Threshold[] = [];

  @Input() measuredEvent: MeasuredEvent;
  @Input() unusedMeasuredEvents: MeasuredEvent[];
  @Output() removeEvent = new EventEmitter();
  @Output() removeOldMeasuredEvent = new EventEmitter();
  newThreshold: Threshold;
  addThresholdDisabled: boolean = false;
  unusedMeasurands: Measurand[];

  @Input()
  set thresholds(thresholds: Threshold[]) {
    this._thresholds = thresholds;
    this.unusedMeasurands = this.measurandsService.getUnusedMeasurands(thresholds);
    this.addThresholdDisabled = this.unusedMeasurands.length < 1;
  };
  get thresholds(): Threshold[] {
    return this._thresholds;
  }

  constructor(private measurandsService: MeasurandService) {
  }

  addThreshold() {
    if (!this.thresholds) {
      return;
    }
    this.addThresholdDisabled = true;

    this.newThreshold = {
      measurand:  this.unusedMeasurands[0],
      lowerBoundary:  0,
      upperBoundary:  0,
      state: "new",
      measuredEvent: this.measuredEvent
    };
  }

  addedThreshold() {
    this.newThreshold = null;
    this.unusedMeasurands.length < 2 ? this.addThresholdDisabled = true : this.addThresholdDisabled = false;
  }

  removeThreshold() {
    if (this.thresholds.length == 1) {
      this.removeOldMeasuredEvent.emit();
    }
    this.addThresholdDisabled = false;
  }

  cancelNewThreshold() {
    this.newThreshold = null;
    this.addThresholdDisabled = false;

  }

  cancelNewMeasuredEvent() {
    this.removeEvent.emit();
  }
}
