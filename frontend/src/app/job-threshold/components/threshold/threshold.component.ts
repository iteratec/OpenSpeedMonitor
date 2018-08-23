import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Threshold} from '../../models/threshold.model';
import {ThresholdRestService} from '../../services/threshold-rest.service';
import {Measurand} from '../../models/measurand.model';
import {ThresholdService} from '../../services/threshold.service';

@Component({
  selector: 'osm-threshold',
  templateUrl: './threshold.component.html',
  styleUrls: ['./threshold.component.scss']
})

export class ThresholdComponent implements OnInit {
  @Input() threshold: Threshold;
  @Input() unusedMeasurands: Measurand [];
  @Output() cancelEvent = new EventEmitter();
  @Output() addedThreshold = new EventEmitter();
  @Output() removeOldThreshold = new EventEmitter();
  selectedMeasurand: string;

  constructor(
    private thresholdRestService: ThresholdRestService,
    private thresholdService: ThresholdService
  ) {
  }

  ngOnInit() {
    if (this.threshold && this.threshold.isNew) {
      this.selectedMeasurand = this.unusedMeasurands[0].translationsKey;
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
    this.thresholdRestService.addThreshold(this.threshold);
    this.addedThreshold.emit();
  }

  cancelNew() {
    this.thresholdService.cancelNew(this.threshold);
    this.cancelEvent.emit();
  }
}
