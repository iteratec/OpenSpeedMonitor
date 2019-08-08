import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {ResultSelectionCommand} from "../../result-selection/models/result-selection-command.model";
import {RemainingResultSelection} from "../../result-selection/models/remaing-result-selection.model";
import {EMPTY, Observable, OperatorFunction} from "rxjs";
import {GetBarchartCommand} from "../../aggregation/models/get-barchart-command.model";
import {catchError} from "rxjs/operators";
import {GetLinechartCommand} from "../models/get-line-chart-command.model";

@Injectable({
  providedIn: 'root'
})
export class LinechartDataService {

  constructor(private http: HttpClient) { }

  fetchLinechartData<T>(resultSelectionCommand: ResultSelectionCommand, remainingResultSelection: RemainingResultSelection, url: string): Observable<T> {
    const getLinechartCommand = LinechartDataService.buildGetLinechartCommand(resultSelectionCommand, remainingResultSelection);
    const params = this.createParams(getLinechartCommand);
    return this.http.get<T>(url, {params: params}).pipe(
      this.handleError()
    )
  }

  private static buildGetLinechartCommand(resultSelectionCommand: ResultSelectionCommand, remainingResultSelection: RemainingResultSelection): GetLinechartCommand {
    return new GetLinechartCommand({
      preconfiguredDashboard: null,
      from: resultSelectionCommand.from,
      to: resultSelectionCommand.to,
      interval: remainingResultSelection.interval,
      measurands: remainingResultSelection.measurands,
      jobGroups: resultSelectionCommand.jobGroupIds,
      pages: resultSelectionCommand.pageIds,
      measuredEvents: resultSelectionCommand.measuredEventIds,
      browsers: resultSelectionCommand.browserIds,
      locations: resultSelectionCommand.locationIds,
      connectivities: resultSelectionCommand.selectedConnectivities,
      deviceTypes: remainingResultSelection.deviceTypes,
      operatingSystems: remainingResultSelection.operatingSystems,
    });
  }

  private createParams(getBarchartCommand: GetBarchartCommand) {
    let params = new HttpParams();

    Object.keys(getBarchartCommand).forEach(key => {
      if(getBarchartCommand[key]) {
        if (key === 'from' || key === 'to') {
          params = params.append(key, getBarchartCommand[key].toISOString());
        } else if (key === 'interval') {
          params = params.append(key, getBarchartCommand[key].toString());
        } else {
          params = params.append(key, JSON.stringify(getBarchartCommand[key]));
        }
      }
    });

    return params;
  }

  private handleError(): OperatorFunction<any, any> {
    return catchError((error) => {
      console.error(error);
      return EMPTY;
    });
  }
}
