import { Component, OnInit, Input } from '@angular/core';
import {Threshold} from '../../service/model/threshold.model';


@Component({
  selector: 'osm-threshold',
  templateUrl: './threshold.component.html',
  styleUrls: ['./threshold.component.css']
})
export class ThresholdComponent implements OnInit {
  @Input() threshold: Threshold;
  constructor() { }

  ngOnInit() {
    console.log("this.threshold: " + JSON.stringify(this.threshold))
  }

}
