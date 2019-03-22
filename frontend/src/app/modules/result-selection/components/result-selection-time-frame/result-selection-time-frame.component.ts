import {Component, Input, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {Caller, ResultSelectionCommand} from "../../models/result-selection-command.model";
import {Chart} from "../../models/result-selection-chart.model";
import {ResultSelectionService} from "../../services/result-selection.service";
import {OsmLangService} from "../../../../services/osm-lang.service";
import {FormControl, FormGroup} from "@angular/forms";
import {DateTimeAdapter, OwlDateTimeComponent} from 'ng-pick-datetime';

@Component({
  selector: 'osm-result-selection-time-frame',
  templateUrl: './result-selection-time-frame.component.html',
  styleUrls: ['./result-selection-time-frame.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ResultSelectionTimeFrameComponent implements OnInit {

  @Input() currentChart: string;
  // @ViewChild("dateTimeTo") dateTimeTo: OwlDateTimeComponent<any>;

  timeSelectionForm = new FormGroup({
    timeFrame: new FormControl(),
  });

  selectableTimeFramesInSeconds: number[] = [0, 3600, 43200, 86400, 259200, 604800, 1209600, 2419200];
  timeFrameInSeconds: number = 0;

  selectedMoments: Date[];

  constructor(private resultSelectionService: ResultSelectionService, private osmLangService: OsmLangService, dateTimeAdapter: DateTimeAdapter<any>) {
    dateTimeAdapter.setLocale(this.osmLangService.getOsmLang() + "-DE");
  }

  ngOnInit() {

    let defaultFrom = new Date();
    let defaultTo = new Date();
    defaultTo.setHours(23, 59, 59, 999);
    defaultFrom.setDate(defaultTo.getDate() - 28);
    defaultFrom.setHours(0, 0, 0, 0);

    this.selectedMoments = [
      defaultFrom,
      defaultTo
    ];

    let defaultResultSelectionCommand = new ResultSelectionCommand({
      from: defaultFrom,
      to: defaultTo,
      caller: Caller.EventResult,
      jobGroupIds: [],
      pageIds: [],
      locationIds: [],
      browserIds: [],
      measuredEventIds: [],
      selectedConnectivities: []
    });

    this.resultSelectionService.loadSelectableData(defaultResultSelectionCommand, Chart[this.currentChart]);
    this.timeFrameInSeconds = this.selectableTimeFramesInSeconds[7];
  }

  selectTimeFrame(): void {
    let from = new Date();
    let to = new Date();

    from.setSeconds(to.getSeconds() - this.timeFrameInSeconds);
    if (this.timeFrameInSeconds >= 259200) {
      to.setHours(23, 59, 59, 999);
      from.setHours(0, 0, 0, 0);
    }
    this.selectedMoments = [from, to];
  }

  updateTimeFrame() {
    this.timeFrameInSeconds = this.selectableTimeFramesInSeconds[0];
    // console.log(this.dateTimeTo);
  }

  updateDate() {

    // console.log(this.dateTimeTo);
  }

  updateName() {
    console.log(this.selectedMoments[0].toISOString());
    console.log(this.selectedMoments[1].toISOString());
  }
}
