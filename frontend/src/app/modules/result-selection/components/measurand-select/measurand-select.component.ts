import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ResultSelectionService} from "../../../../services/result-selection.service";
import {ReplaySubject} from "rxjs";
import {ResponseWithLoadingState} from "../../../../models/response-with-loading-state.model";
import {MeasurandGroup, SelectableMeasurand} from "../../../../models/measurand.model";

@Component({
  selector: 'osm-measurand-select',
  templateUrl: './measurand-select.component.html',
  styleUrls: ['./measurand-select.component.scss']
})
export class MeasurandSelectComponent implements OnInit {
  @Input() selectedMeasurand: SelectableMeasurand;
  @Output() onSelect: EventEmitter<SelectableMeasurand> = new EventEmitter<SelectableMeasurand>();

  selectableMeasurandGroups: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>>[];

  constructor(private resultSelectionService: ResultSelectionService) {
    this.selectableMeasurandGroups = [
      this.resultSelectionService.loadTimes$,
      this.resultSelectionService.heroTimings$,
      this.resultSelectionService.userTimings$
    ];
  }

  ngOnInit() {
    // this.selectedMeasurand = {name: "frontend.de.iteratec.isr.measurand.DOC_COMPLETE_TIME", id: "DOC_COMPLETE_TIME"}
    // this.selectableMeasurandGroups.entries().next((group: ResponseWithLoadingState<MeasurandGroup>) => {
    //   group.data.values.filter((measurand: SelectableMeasurand) => measurand.name == this.initialValue)
    //     .reduce((measurand: SelectableMeasurand) => {
    //       this.selectedMeasurand = measurand;
    //       return measurand
    //     })
    // })
  }

  selectMeasurand() {
    console.log(`measurand in selectMeasurand=${JSON.stringify(this.selectedMeasurand)}`);
    console.log(`measurand in selectMeasurand=${JSON.stringify(this.selectedMeasurand.id)}`);
    console.log(`measurand in selectMeasurand=${JSON.stringify(this.selectedMeasurand.name)}`);
    this.onSelect.emit(this.selectedMeasurand);
  }

}
