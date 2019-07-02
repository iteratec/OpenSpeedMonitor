import {Component, Input, OnInit} from '@angular/core';
import {Subject} from "rxjs";

@Component({
  selector: 'osm-result-selection-reset',
  templateUrl: './reset.component.html',
  styleUrls: ['./reset.component.scss']
})
export class ResetComponent implements OnInit {

  @Input() clickEvent: Subject<void> = new Subject<void>();

  constructor() { }

  ngOnInit() {
  }

  emitResetEventToComponent() {
    this.clickEvent.next();
  }
}
