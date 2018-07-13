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
  @Input()  actualMeasurands: Measurand [];
  @Output() cancelEvent = new EventEmitter();
  @Output() removeOldThreshold = new EventEmitter();
  //measurandList: Measurand[];
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

  constructor(private thresholdRestService: ThresholdRestService) {
    /*this.thresholdRestService.measurands$.subscribe((next: Measurand[]) => {
      this.measurandList = next;
    } );*/

  }

  ngOnInit() {
    this.firstUpperBoundary = this.threshold.upperBoundary;
    this.firstLowerBoundary = this.threshold.lowerBoundary;
    if (this.threshold.measuredEvent.state == "new") {
      this.selectedMeasuredEvent = this.measuredEventList[0];
     /* this.selectedMeasurand = this.actualMeasurands[0].name;
      console.log("THRESHOLD newMeasuredEvent this.actualMeasurands: " + JSON.stringify(this.actualMeasurands));
      console.log("THRESHOLD newMeasuredEvent this.actualMeasurands[0].name: " + JSON.stringify(this.actualMeasurands[0].name));*/
    }
    if (this.threshold.state == "new") {
      console.log("THRESHOLD newThreshold this.actualMeasurands: " + JSON.stringify(this.actualMeasurands));

      this.selectedMeasurand = this.actualMeasurands[0].name;
      this.leftButtonLabelDisable = true;
    }
  }

  delete(thresholdID) {
    if (this.deleteConfirmation) {
      this.deleteConfirmation = !this.deleteConfirmation;
      this.thresholdRestService.deleteThreshold(thresholdID);
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
    console.log("EDIT");
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
    this.threshold.measurand.name = this.selectedMeasurand;
    if (this.threshold.measuredEvent.state == "new") {
      this.threshold.measuredEvent = this.selectedMeasuredEvent;
      this.thresholdRestService.addThreshold(this.threshold);
    }
    else {
      this.thresholdRestService.addThreshold(this.threshold);
    }
  }

  onKey(event: any) { // without type info
    this.value = event.target.value ;
    console.log(this.value);
    if (this.value > 0) {
      this.leftButtonLabelDisable = false;
    }
  }

  remove() {
    console.log("THRESHOLD REMOVE");
    this.cancelEvent.emit();
  }
}
