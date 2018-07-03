import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import {Threshold} from '../../service/model/threshold.model';
import { ThresholdRestService } from '../../service/rest/threshold-rest.service';




@Component({
  selector: 'osm-threshold',
  templateUrl: './threshold.component.html',
  styleUrls: ['./threshold.component.css']
})
export class ThresholdComponent implements OnInit {
  @Input() threshold: Threshold;
  @Output()
  deleteThresh: EventEmitter<String> = new EventEmitter<String>(); //creating an output event
  constructor(private thresholdRestService: ThresholdRestService) { }

  ngOnInit() {
    console.log("this.threshold: " + JSON.stringify(this.threshold))
  }

  deleteThreshold(thresholdId){
    console.log("threshold Componet: " + JSON.stringify(thresholdId))
    this.deleteThresh.emit(thresholdId); //emmiting the event.
  }

}
