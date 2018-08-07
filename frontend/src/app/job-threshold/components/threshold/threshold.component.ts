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
  allowInput = false;
  deleteConfirmation = false;
  leftButtonLabel = "Editieren";
  rightButtonLabel = "Löschen";
  firstUpperBoundary: number;
  firstLowerBoundary: number;
  leftButtonLabelDisable: boolean = false;
  lowerInput: number = 0;
  upperInput: number = 0;

  constructor(private thresholdRestService: ThresholdRestService) {
  }

  ngOnInit() {
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

  delete(threshold: Threshold) {

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

  cancelDelete() {
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
    );
  }

  onLowerInput(event: any) {
    this.lowerInput = event.target.value;
    if (this.upperInput > 0 && this.upperInput > this.lowerInput) {
      this.leftButtonLabelDisable = false;
    } else {
      this.leftButtonLabelDisable = true;
    }
  }

  onUpperInput(event: any) {
    this.upperInput = event.target.value;
    if (this.upperInput > 0 && this.upperInput > this.lowerInput) {
      this.leftButtonLabelDisable = false;
    } else {
      this.leftButtonLabelDisable = true;
    }
  }

  saveNew(obj) {
    console.log("THRESHOLD saveNew ob: " + JSON.stringify(obj))
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
