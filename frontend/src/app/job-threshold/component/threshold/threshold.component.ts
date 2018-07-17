import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import {Threshold} from '../../service/model/threshold.model';
import { ThresholdRestService } from '../../service/rest/threshold-rest.service';
import {Measurand} from "../../service/model/measurand.model";
import {MeasuredEvent} from '../../service/model/measured-event.model';

@Component({
  selector: 'osm-threshold',
  templateUrl: './threshold.component.html',
  styleUrls: ['./threshold.component.css']
})

export class ThresholdComponent implements OnInit{
  @Input() threshold: Threshold;
  @Input() measuredEventList: MeasuredEvent[];
  @Input()  actualMeasurandList: Measurand [];
  @Output() cancelEvent = new EventEmitter();
  @Output() removeOldThreshold = new EventEmitter();
  @Output() savedThreshold = new EventEmitter();
  selectedMeasuredEvent: MeasuredEvent;
  selectedMeasurand: string;
  allowInput = false;
  deleteConfirmation = false;
  leftButtonLabel= "Editieren";
  rightButtonLabel= "Löschen";
  firstUpperBoundary: number;
  firstLowerBoundary: number;
  leftButtonLabelDisable: boolean = false;
  value: number = 0;

  constructor(private thresholdRestService: ThresholdRestService) {}

  ngOnInit() {
    console.log("THRESHOLD");
    this.firstUpperBoundary = this.threshold.upperBoundary;
    this.firstLowerBoundary = this.threshold.lowerBoundary;
    if (this.threshold.measuredEvent.state == "new") {
      this.selectedMeasuredEvent = this.measuredEventList[0];
    }
    if (this.threshold.state == "new") {
      this.selectedMeasurand = this.actualMeasurandList[0].name;
      this.leftButtonLabelDisable = true;
    }
  }

  delete(threshold) {
    if (this.deleteConfirmation) {
      this.deleteConfirmation = !this.deleteConfirmation;
      this.thresholdRestService.deleteThreshold(threshold);
      this.removeOldThreshold.emit();
    } else {
      this.rightButtonLabel == "Löschen"
        ? (
        this.deleteConfirmation = !this.deleteConfirmation,
          this.rightButtonLabel = "Nein",
          this.leftButtonLabel = "Ja"
      )
        : (
        this.allowInput = !this.allowInput,
          this.threshold.upperBoundary = this.firstUpperBoundary,
          this.threshold.lowerBoundary = this.firstLowerBoundary,
          this.rightButtonLabel = "Löschen",
          this.leftButtonLabel = "Editieren"
      )
    }
  }

  cancel() {
    this.deleteConfirmation = !this.deleteConfirmation;
    this.rightButtonLabel = "Löschen";
    this.leftButtonLabel = "Editieren";
  }

  edit() {
    this.allowInput = !this.allowInput;
    this.allowInput
      ? (
      this.rightButtonLabel = "Zurücksetzen",
        this.leftButtonLabel = "Übernehmen"
      )
      : (
      this.rightButtonLabel = "Löschen",
        this.leftButtonLabel = "Editieren",
          this.thresholdRestService.editThreshold(this.threshold)
      ) ;
  }

  save() {
    this.savedThreshold.emit();
    this.threshold.measurand.name = this.selectedMeasurand;
    if (this.threshold.measuredEvent.state == "new") {
      this.threshold.measuredEvent = this.selectedMeasuredEvent;
      this.thresholdRestService.addThreshold(this.threshold);
    }
    else {
      this.thresholdRestService.addThreshold(this.threshold);
    }
  }

  onKey(event: any) {
    this.value = event.target.value ;
    if (this.value > 0) {
      this.leftButtonLabelDisable = false;
    }
  }

  remove() {
    this.cancelEvent.emit();
  }
}
