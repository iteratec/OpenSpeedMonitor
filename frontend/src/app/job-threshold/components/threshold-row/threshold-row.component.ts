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
  allowInput: boolean = false;
  deleteConfirmation: boolean = false;

  constructor() {
  }

  ngOnInit() {
    this.originalUpperBoundary = this.upperBoundary;
    this.originalLowerBoundary = this.lowerBoundary;
    if (this.thresholdState == "new") {
      this.leftButtonLabelDisable = true;
      this.leftButtonLabel = "Speichern";
      this.rightButtonLabel = "Entfernen";
    } else {
      this.leftButtonLabel = "Editieren";
      this.rightButtonLabel = "Löschen";
    }
  }


  edit() {
    this.allowInput = !this.allowInput;
    if (this.allowInput) {
      this.rightButtonLabel = "Zurücksetzen";
      this.leftButtonLabel = "Übernehmen";
    } else {
      this.rightButtonLabel = "Löschen";
      this.leftButtonLabel = "Editieren";
      this.editThreshold.emit();
    }
  }

  delete() {
    if (this.deleteConfirmation) {
      this.deleteConfirmation = !this.deleteConfirmation;
      this.deleteThreshold.emit();
    } else {
      if (this.rightButtonLabel == "Löschen") {
        this.deleteConfirmation = !this.deleteConfirmation;
        this.rightButtonLabel = "Nein";
        this.leftButtonLabel = "Ja";
      } else {
        this.allowInput = !this.allowInput;
        this.upperBoundary = this.originalUpperBoundary;
        this.lowerBoundary = this.originalLowerBoundary;
        this.rightButtonLabel = "Löschen";
        this.leftButtonLabel = "Editieren";
      }
    }
  }

  cancelDelete() {
    this.deleteConfirmation = !this.deleteConfirmation;
    this.rightButtonLabel = "Löschen";
    this.leftButtonLabel = "Editieren";
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
