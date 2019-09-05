import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {ResultSelectionCommand} from "../../result-selection/models/result-selection-command.model";
import {RemainingResultSelection} from "../../result-selection/models/remaing-result-selection.model";
import {EMPTY, Observable, OperatorFunction} from "rxjs";
import {catchError} from "rxjs/operators";
import {GetViolinchartCommand} from '../models/get-violin-chart-command.model';

@Injectable({
  providedIn: 'root'
})
export class ViolinchartDataService {

  constructor(private http: HttpClient) { }

  fetchDistributionData<T>(resultSelectionCommand: ResultSelectionCommand, remainingResultSelection: RemainingResultSelection, url: string): Observable<T> {
    const cmd: GetViolinchartCommand = this.buildCommand(resultSelectionCommand, remainingResultSelection);
    console.log("cmd: " + JSON.stringify(cmd));
    const params = this.createParams(cmd);
    return this.http.get<T>(url, {params: params}).pipe(
      this.handleError()
    )
  }

  private buildCommand(resultSelectionCommand: ResultSelectionCommand, remainingResultSelection: RemainingResultSelection): GetViolinchartCommand {
    return new GetViolinchartCommand({
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
      performanceAspectTypes: remainingResultSelection.performanceAspectTypes
    });

  }

  private createParams(getViolinchartCommand: GetViolinchartCommand) {
    let params = new HttpParams();

    Object.keys(getViolinchartCommand).forEach(key => {
      if(getViolinchartCommand[key]) {
        if (key === 'from' || key === 'to') {
          params = params.append(key, getViolinchartCommand[key].toISOString());
        } else if (key === 'interval') {
          params = params.append(key, getViolinchartCommand[key].toString());
        } else {
          params = params.append(key, JSON.stringify(getViolinchartCommand[key]));
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
