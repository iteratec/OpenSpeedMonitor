import { Component, OnInit, Input } from '@angular/core';
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
  measurandList: Measurand[];
  measuredEventList: MeasuredEvent[];
  selectedMeasuredEvent: MeasuredEvent;
  selectedOption: string;
  allowInput = false;
  leftButtonLabel= "Editieren";
  rightButtonLabel= "Löschen";
  firstUpperBoundary: number;
  firstLowerBoundary: number;

  constructor(private thresholdRestService: ThresholdRestService) {
    this.thresholdRestService.measurands$.subscribe((next: Measurand[]) => {
      this.measurandList = next;
    } );
    this.thresholdRestService.measuredEvents$.subscribe((next: MeasuredEvent[]) => {
      this.measuredEventList = next;
    } );
  }

  ngOnInit() {
    console.log("this.allowthresholdAdd: " + this.allowthresholdAdd);
    console.log("Threshold.component ngOninit this.threshold: " + JSON.stringify(this.threshold));
    this.firstUpperBoundary = this.threshold.upperBoundary;
    this.firstLowerBoundary = this.threshold.lowerBoundary;

    /*if (this.threshold.state == "new") {
      this.leftButtonLabel = "Speichern";
    }*/
  }

  delete(thresholdID) {
    console.log("DELETE");

    this.rightButtonLabel == "Löschen"
      ? this.thresholdRestService.deleteThreshold(thresholdID)
      : (
        this.allowInput = !this.allowInput,
          this.threshold.upperBoundary = this.firstUpperBoundary,
          this.threshold.lowerBoundary = this.firstLowerBoundary,
          this.rightButtonLabel = "Löschen",
          this.leftButtonLabel = "Editieren"
      )




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
    this.threshold.measurand.name = this.selectedOption;
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
  }


}
