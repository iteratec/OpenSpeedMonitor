import { Component, OnInit, Input } from '@angular/core';
import {MeasuredEvent} from '../../service/model/measured-event.model';

@Component({
  selector: 'osm-measured-event',
  templateUrl: './measured-event.component.html',
  styleUrls: ['./measured-event.component.css']
})
export class MeasuredEventComponent implements OnInit {
  @Input() measuredEvent: MeasuredEvent;
  constructor() { }

  ngOnInit() {
  }

}
