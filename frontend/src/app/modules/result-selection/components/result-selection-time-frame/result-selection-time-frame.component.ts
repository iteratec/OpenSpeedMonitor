import {Component, ElementRef, Input, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {formatDate} from "@angular/common";
import {Caller, ResultSelectionCommand} from "../../models/result-selection-command.model";
import {Chart} from "../../models/result-selection-chart.model";
import {ResultSelectionService} from "../../services/result-selection.service";
import {FlatpickrOptions, Ng2FlatpickrComponent} from "ng2-flatpickr";
import rangePlugin from 'flatpickr/dist/plugins/rangePlugin'
import German from 'flatpickr/dist/l10n/de'
import {OsmLangService} from "../../../../services/osm-lang.service";
import {FormControl, FormGroup} from "@angular/forms";

@Component({
  selector: 'osm-result-selection-time-frame',
  templateUrl: './result-selection-time-frame.component.html',
  styleUrls: ['./result-selection-time-frame.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ResultSelectionTimeFrameComponent implements OnInit {

  @Input() currentChart: string;
  @ViewChild("resultSelectionFlatpickr") resultSelectionFlatpickr: Ng2FlatpickrComponent;
  @ViewChild("resultSelectionFlatpickrSecondField") resultSelectionFlatpickrSecondField: ElementRef;

  from: Date;
  to: Date;

  timeSelectionForm = new FormGroup({
    timeFrame: new FormControl(),
  });

  selectableTimeFramesInSeconds: number[] = [0, 3600, 43200, 86400, 259200, 604800, 1209600, 2419200];
  timeFrameInSeconds: number = 0;

  resultSelectionFlatpickrOptions: FlatpickrOptions;

  constructor(private resultSelectionService: ResultSelectionService, private osmLangService: OsmLangService) {
  }

  ngOnInit() {
    let defaultFrom = new Date();
    let defaultTo = new Date();
    defaultTo.setHours(23, 59, 59, 999);
    defaultFrom.setDate(defaultTo.getDate() - 28);
    defaultFrom.setHours(0, 0, 0, 0);

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

    this.from = defaultFrom;
    this.to = defaultTo;

    this.updateFlatpickrConfig();
    this.timeFrameInSeconds = this.selectableTimeFramesInSeconds[7];
    this.timeSelectionForm.setValue({'timeFrame': [this.from, this.to]});
    this.resultSelectionFlatpickrSecondField.nativeElement.value = formatDate(this.to, 'dd.LL.yyyy HH:mm', 'en');
  }

  private updateFlatpickrConfig(): void {
    this.resultSelectionFlatpickrOptions = {
      dateFormat: 'd.m.Y H:i',
      enableTime: true,
      time_24hr: true,
      defaultDate: this.from,
      ...(this.osmLangService.getOsmLang() === 'de' && { locale: German.de }),
      plugins: [rangePlugin({ input: "#resultSelectionFlatpickrSecondField" })],
    };
  }

  selectTimeFrame(): void {
    this.from = new Date();
    this.to = new Date();
    this.from.setSeconds(this.to.getSeconds() - this.timeFrameInSeconds);
    if (this.timeFrameInSeconds >= 259200) {
      this.to.setHours(23, 59, 59, 999);
      this.from.setHours(0, 0, 0, 0);
    }

    this.timeSelectionForm.setValue({'timeFrame': [this.from, this.to]});
    this.resultSelectionFlatpickr.flatpickrElement.nativeElement._flatpickr.setDate(this.from);
    this.resultSelectionFlatpickrSecondField.nativeElement.value = formatDate(this.to, 'dd.LL.yyyy HH:mm', 'en');
  }

  updateName() {
    console.log(this.timeSelectionForm.value);
  }
}
