import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {Threshold} from "../../models/threshold.model";
import {ThresholdRestService} from "../../services/threshold-rest.service";
import {Measurand} from "../../models/measurand.model";
import {MeasuredEvent} from "../../models/measured-event.model";

@Component({
  selector: 'osm-threshold',
  templateUrl: './threshold.component.html',
  styleUrls: ['./threshold.component.css']
})

export class ThresholdComponent implements OnInit {
  @Input() threshold: Threshold;
  @Input() measuredEventList: MeasuredEvent[];
  @Input() actualMeasurandList: Measurand [];
  @Output() cancelEvent = new EventEmitter();
  @Output() addedMeasuredEvent = new EventEmitter();
  @Output() addedThreshold = new EventEmitter();
  @Output() removeOldThreshold = new EventEmitter();
  selectedMeasuredEvent: MeasuredEvent;
  selectedMeasurand: string;

  constructor(private thresholdRestService: ThresholdRestService) {
  }

  ngOnInit() {
    if (this.threshold.measuredEvent.state == "new") {
      this.selectedMeasuredEvent = this.measuredEventList[0];
    }
    if (this.threshold.state == "new") {
      this.selectedMeasurand = this.actualMeasurandList[0].name;
    }
  }

  edit() {
    this.thresholdRestService.editThreshold(this.threshold)
  }

  delete() {
    this.thresholdRestService.deleteThreshold(this.threshold);
  }

  saveNew(obj) {
    this.threshold.measurand.name = this.selectedMeasurand;
    this.threshold.lowerBoundary = obj.lowerBoundary;
    this.threshold.upperBoundary = obj.upperBoundary;
    if (this.threshold.measuredEvent.state == "new") {
      this.threshold.measuredEvent = this.selectedMeasuredEvent;
      this.threshold.measuredEvent.state = "new";
      this.thresholdRestService.addThreshold(this.threshold);
      this.addedMeasuredEvent.emit()
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
