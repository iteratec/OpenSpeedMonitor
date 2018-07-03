import { Component, OnInit, Input } from '@angular/core';
import {MeasuredEvent} from '../../service/model/measured-event.model';
import {Threshold} from '../../service/model/threshold.model';

@Component({
  selector: 'osm-measured-event',
  templateUrl: './measured-event.component.html',
  styleUrls: ['./measured-event.component.css']
})
export class MeasuredEventComponent implements OnInit {
  @Input() measuredEvent: MeasuredEvent;
  @Input() thresholds: Threshold[];
  constructor() { }

  ngOnInit() {
    console.log("this.measuredEvent: " + JSON.stringify(this.measuredEvent))
  }

}
