import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";

@Component({
  selector: 'osm-threshold-row',
  templateUrl: './threshold-row.component.html',
  styleUrls: ['./threshold-row.component.scss']
})
export class ThresholdRowComponent implements OnInit {
  @Input() state: string;
  @Input() lowerBoundary: number;
  @Input() upperBoundary: number;
  @Output() saveNew = new EventEmitter();
  @Output() cancelNew = new EventEmitter();
  /** Variables fort input validation*/
  lowerInput: number = 0;
  upperInput: number = 0;

  leftButtonLabelDisable: boolean = false;

  constructor() {
  }

  ngOnInit() {
    if (this.state == "new") {
      this.leftButtonLabelDisable = true;
    }
  }

  save() {
    console.log("THRESHOLDROW save");
    let self = this;
    this.saveNew.emit({lowerBoundary: this.lowerBoundary, upperBoundary: this.upperBoundary});
  }

  onLowerInput(event: any) {
    this.lowerInput = event.target.value;
    if (this.upperInput > 0 && this.upperInput > this.lowerInput) {
      this.leftButtonLabelDisable = false;
    } else {
      this.leftButtonLabelDisable = true;
    }
  }

  onUpperInput(event: any) {
    this.upperInput = event.target.value;
    if (this.upperInput > 0 && this.upperInput > this.lowerInput) {
      this.leftButtonLabelDisable = false;
    } else {
      this.leftButtonLabelDisable = true;
    }
  }

  cancel() {
    this.cancelNew.emit();
  }


}
