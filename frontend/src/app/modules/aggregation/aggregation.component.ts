import {Component, OnInit} from '@angular/core';
import {AggregationService} from "./services/aggregation.service";
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {GetBarchartCommand} from "./models/get-barchart-command.model";
import {ResultSelectionCommand} from "../result-selection/models/result-selection-command.model";
import {URL} from "../../enums/url.enum";

@Component({
  selector: 'osm-aggregation',
  templateUrl: './aggregation.component.html',
  styleUrls: ['./aggregation.component.scss']
})
export class AggregationComponent implements OnInit {

  constructor(private dataService: AggregationService, private resultSelectionStore: ResultSelectionStore) { }

  ngOnInit() {
  }

  getBarchartData() {
    let getBarchartCommand: GetBarchartCommand = AggregationComponent.createGetBarchartCommand(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingGetBarchartCommand);
    this.dataService.fetchBarChartData<any>( getBarchartCommand, URL.AGGREGATION_BARCHART_DATA).subscribe(result => console.log(result));
  }

  private static createGetBarchartCommand(resultSelectionCommand: ResultSelectionCommand, remainingGetBarchartCommand: GetBarchartCommand): GetBarchartCommand {
    return new GetBarchartCommand({
      from: resultSelectionCommand.from,
      to: resultSelectionCommand.to,
      fromComparative: remainingGetBarchartCommand.fromComparative,
      toComparative: remainingGetBarchartCommand.toComparative,
      pages: resultSelectionCommand.pageIds,
      jobGroups: resultSelectionCommand.jobGroupIds,
      measurands: remainingGetBarchartCommand.measurands,
      browsers: resultSelectionCommand.browserIds,
      deviceTypes: remainingGetBarchartCommand.deviceTypes,
      operatingSystems: remainingGetBarchartCommand.operatingSystems,
      aggregationValue: "avg"
    });
  }
}
