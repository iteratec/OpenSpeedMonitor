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
export class ThresholdComponent implements OnInit {
  @Input() threshold: Threshold;
  @Input() allowthresholdAdd: boolean;
  @Input() measuredEventList: MeasuredEvent[];
  @Output() removeEvent = new EventEmitter();
  measurandList: Measurand[];
  selectedMeasuredEvent: MeasuredEvent;
  selectedMeasurand: string;
  allowInput = false;
  deleteConfirmation = false;
  leftButtonLabel= "Editieren";
  rightButtonLabel= "Löschen";
  firstUpperBoundary: number;
  firstLowerBoundary: number;

  constructor(private thresholdRestService: ThresholdRestService) {
    this.thresholdRestService.measurands$.subscribe((next: Measurand[]) => {
      this.measurandList = next;
    } );

  }

  ngOnInit() {
    this.firstUpperBoundary = this.threshold.upperBoundary;
    this.firstLowerBoundary = this.threshold.lowerBoundary;
    this.selectedMeasuredEvent = this.measuredEventList[0];
    this.selectedMeasurand = this.measurandList[0].name;
  }

  delete(thresholdID) {
    console.log("DELETE");
    //this.thresholdRestService.deleteThreshold(thresholdID)

    if (this.deleteConfirmation) {
      this.deleteConfirmation = !this.deleteConfirmation;
      this.thresholdRestService.deleteThreshold(thresholdID);
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
    console.log("SAVE");
    this.threshold.measurand.name = this.selectedMeasurand;
    this.selectedMeasuredEvent
      ? (
        this.threshold.measuredEvent = this.selectedMeasuredEvent,
          this.thresholdRestService.addThreshold(this.threshold)
      ):(
        this.thresholdRestService.addThreshold(this.threshold)
      );
  }

  remove() {
    console.log("REMOVE");
    this.removeEvent.emit();
  }
}
