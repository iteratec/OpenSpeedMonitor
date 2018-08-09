import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";

@Component({
  selector: 'osm-threshold-row',
  templateUrl: './threshold-row.component.html',
  styleUrls: ['./threshold-row.component.scss']
})
export class ThresholdRowComponent implements OnInit {
  @Input() thresholdState: string;
  @Input() lowerBoundary: number;
  @Input() upperBoundary: number;
  @Output() deleteThreshold = new EventEmitter();
  @Output() editThreshold = new EventEmitter();
  @Output() saveNew = new EventEmitter();
  @Output() cancelNew = new EventEmitter();

  /* Variables fort input validation*/
  lowerInput: number = 0;
  upperInput: number = 0;

  /*Variables to determine buttons label*/
  leftButtonLabel: string;
  rightButtonLabel: string;

  /* Variables to recover original boundary value*/
  originalUpperBoundary: number;
  originalLowerBoundary: number;

  leftButtonLabelDisable: boolean = false;  //disable for save and accept buttons
  disableInput: boolean = true;
  deleteConfirmation: boolean = false;

  constructor() {
  }

  ngOnInit() {
    this.originalUpperBoundary = this.upperBoundary;
    this.originalLowerBoundary = this.lowerBoundary;
    if (this.thresholdState == "new") {
      this.disableInput = false;
      this.leftButtonLabelDisable = true;
      this.leftButtonLabel = "frontend.job.threshold.save";
      this.rightButtonLabel = "frontend.job.threshold.remove";
    } else {
      this.leftButtonLabel = "frontend.job.threshold.edit";
      this.rightButtonLabel = "frontend.job.threshold.delete";
    }
  }


  edit() {
    this.disableInput = !this.disableInput;
    if (!this.disableInput) {
      this.rightButtonLabel = "frontend.job.threshold.discard";
      this.leftButtonLabel = "frontend.job.threshold.submit";
    } else {
      this.rightButtonLabel = "frontend.job.threshold.delete";
      this.leftButtonLabel = "frontend.job.threshold.edit";
      this.editThreshold.emit({lowerBoundary: this.lowerBoundary, upperBoundary: this.upperBoundary});
    }
  }

  delete() {
    if (this.deleteConfirmation) {
      this.deleteConfirmation = !this.deleteConfirmation;
      this.deleteThreshold.emit();
    } else {
      if (this.rightButtonLabel == "frontend.job.threshold.delete") {
        this.deleteConfirmation = !this.deleteConfirmation;
        this.rightButtonLabel = "frontend.job.threshold.deleteNo";
        this.leftButtonLabel = "frontend.job.threshold.deleteYes";
      } else {
        this.disableInput = !this.disableInput;
        this.upperBoundary = this.originalUpperBoundary;
        this.lowerBoundary = this.originalLowerBoundary;
        this.rightButtonLabel = "frontend.job.threshold.delete";
        this.leftButtonLabel = "frontend.job.threshold.edit";
      }
    }
  }

  cancelDelete() {
    this.deleteConfirmation = !this.deleteConfirmation;
    this.rightButtonLabel = "frontend.job.threshold.delete";
    this.leftButtonLabel = "frontend.job.threshold.edit";
  }

  save() {
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
