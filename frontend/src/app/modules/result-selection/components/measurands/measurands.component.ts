import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {SelectableMeasurand} from "../../../../models/measurand.model";
import {ReplaySubject} from "rxjs";
import {ResultSelectionService} from "../../services/result-selection.service";

@Component({
  selector: 'osm-measurands',
  templateUrl: './measurands.component.html',
  styleUrls: ['./measurands.component.scss']
})
export class MeasurandsComponent implements OnInit {

  selectedMeasurands: SelectableMeasurand[];
  defaultValue: SelectableMeasurand;

  constructor(private resultSelectionService: ResultSelectionService) {
    this.resultSelectionService.loadTimes$.subscribe(next => {
      if(next) {
        this.defaultValue = next.data.values[0];
        this.selectedMeasurands = [this.defaultValue];
      }
    });
  }

  ngOnInit() {
  }

  selectMeasurand(index: number, measurand: SelectableMeasurand) {
    this.selectedMeasurands[index] = measurand;
  }

  addMeasurandField() {
    this.selectedMeasurands.push(this.defaultValue);
  }

  removeMeasurandField(index: number) {
    this.selectedMeasurands.splice(index, 1);
  }

  trackByFn(index: number, item: any) {
    return index;
  }
}
