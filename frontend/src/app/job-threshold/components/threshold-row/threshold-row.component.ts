import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'osm-threshold-row',
  templateUrl: './threshold-row.component.html',
  styleUrls: ['./threshold-row.component.scss']
})
export class ThresholdRowComponent implements OnInit {
  @Input() isNew: boolean;
  @Input() lowerBoundary: number;
  @Input() upperBoundary: number;
  @Output() deleteThreshold = new EventEmitter();
  @Output() editThreshold = new EventEmitter();
  @Output() saveNew = new EventEmitter();
  @Output() cancelNew = new EventEmitter();

  originalUpperBoundary: number;
  originalLowerBoundary: number;

  leftButtonLabelDisable: boolean = false;
  editMode: boolean = false;
  deleteConfirmation: boolean = false;

  constructor() {
  }

  ngOnInit() {
    this.originalUpperBoundary = this.upperBoundary;
    this.originalLowerBoundary = this.lowerBoundary;
    if (this.isNew) {
      this.editMode = true;
      this.leftButtonLabelDisable = true;
    }
  }

  edit() {
    this.editMode = true;
  }

  delete() {
    if (!this.deleteConfirmation) {
      this.deleteConfirmation = true;
    } else {
      this.deleteConfirmation = false;
      this.deleteThreshold.emit();
    }
  }

  cancelDelete() {
    this.deleteConfirmation = !this.deleteConfirmation;
  }

  save() {
    const newBoundaries = {lowerBoundary: this.lowerBoundary, upperBoundary: this.upperBoundary};
    if (this.isNew) {
      this.saveNew.emit(newBoundaries);
    } else {
      this.editMode = false;
      this.editThreshold.emit(newBoundaries);
    }
  }

  validateInput() {
    this.leftButtonLabelDisable = !(this.upperBoundary > 0 && this.upperBoundary > this.lowerBoundary);
  }

  cancel() {
    if (this.isNew) {
      this.cancelNew.emit();
    } else {
      this.editMode = false;
      this.upperBoundary = this.originalUpperBoundary;
      this.lowerBoundary = this.originalLowerBoundary;
    }
  }


}
