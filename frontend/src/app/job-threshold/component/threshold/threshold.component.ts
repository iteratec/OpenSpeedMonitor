import { Component, OnInit, Input } from '@angular/core';
import {Threshold} from '../../service/model/threshold.model';
import { ThresholdRestService } from '../../service/rest/threshold-rest.service';




@Component({
  selector: 'osm-threshold',
  templateUrl: './threshold.component.html',
  styleUrls: ['./threshold.component.css']
})
export class ThresholdComponent implements OnInit {
  @Input() threshold: Threshold;
  allowInput = false;
  editButtonLabel= "Editieren";

  constructor(private thresholdRestService: ThresholdRestService) { }

  ngOnInit() {
    console.log("this.threshold: " + JSON.stringify(this.threshold));
  }

  delete(thresholdID) {
    console.log("DELETE");
    this.thresholdRestService.deleteThreshold(thresholdID);
    //this.deleteThresh.emit(thresholdId); //emmiting the event.
  }

  edit() {
    console.log("EDIT");
    this.allowInput = !this.allowInput;
    this.allowInput
      ? this.editButtonLabel = "Übernehmen"
      : (
      this.editButtonLabel = "Editieren",
        this.thresholdRestService.editThreshold(this.threshold)
    ) ;
    console.log(" this.threshold: " + this.threshold.lowerBoundary);

  }

}
