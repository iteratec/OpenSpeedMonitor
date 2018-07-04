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
  constructor(private thresholdRestService: ThresholdRestService) { }

  ngOnInit() {
    console.log("this.threshold: " + JSON.stringify(this.threshold));
  }

  delete() {
    console.log("DELETE");
    //this.thresholdRestService.deleteThreshold(5);
    //this.deleteThresh.emit(thresholdId); //emmiting the event.
  }

}
