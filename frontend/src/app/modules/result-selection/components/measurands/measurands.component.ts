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

  @ViewChild('additionalMeasurands') additionalMeasurands: ElementRef;
  selectedMetric$: ReplaySubject<SelectableMeasurand[]> = new ReplaySubject<SelectableMeasurand[]>(null);
  selectedMeasurands: string[];
  selectedMeasurandsTest: SelectableMeasurand[];

  constructor(private resultSelectionService: ResultSelectionService) {  }

  ngOnInit() {
    this.selectedMeasurands = ["DOC_COMPLETE_TIME"];
    this.selectedMeasurandsTest = [];
  }

  selectMeasurandForAspect(index: number, measurand: SelectableMeasurand) {
    // this.selectedMeasurands[index] = measurand.id;
    this.selectedMeasurandsTest[index] = measurand;
    // this.selectedMetric$[index].next(measurand);
    console.log(this.selectedMeasurands);
    console.log(this.selectedMeasurandsTest);
    console.log(index);
  }

  addMeasurandField() {
    console.log(this.selectedMeasurandsTest);
    this.selectedMeasurands.push("DOC_COMPLETE_TIME");

  }

  private updateSelectedMetric() {

  }
}
