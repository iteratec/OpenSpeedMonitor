import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {EMPTY, Observable, OperatorFunction} from "rxjs";
import {catchError} from "rxjs/operators";
import {GetBarchartCommand, RemainingGetBarchartCommand} from "../models/get-barchart-command.model";
import {ResultSelectionCommand} from "../../result-selection/models/result-selection-command.model";

@Injectable()
export class BarchartDataService {

  constructor(private http: HttpClient) { }

  fetchBarchartData<T>(resultSelectionCommand: ResultSelectionCommand, remainingGetBarchartCommand: RemainingGetBarchartCommand, aggregationValue: (string | number), url: string): Observable<T> {
    const getBarchartCommand = BarchartDataService.buildGetBarchartCommand(resultSelectionCommand, remainingGetBarchartCommand, aggregationValue);
    const params = this.createParams(getBarchartCommand);
    return this.http.get<T>(url, {params: params}).pipe(
      this.handleError()
    )
  }

  private static buildGetBarchartCommand(resultSelectionCommand: ResultSelectionCommand, remainingGetBarchartCommand: RemainingGetBarchartCommand, aggregationValue: (string | number)): GetBarchartCommand {
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
      aggregationValue: aggregationValue
    });
  }

  private createParams(getBarchartCommand: GetBarchartCommand) {
    let params = new HttpParams();

    Object.keys(getBarchartCommand).forEach(key => {
      if(getBarchartCommand[key]) {
        if (key === 'from' || key === 'to' || key === 'fromComparative' || key === 'toComparative') {
          params = params.append(key, getBarchartCommand[key].toISOString());
        } else if (key === 'aggregationValue') {
          params = params.append(key, getBarchartCommand[key].toString());
        } else {
          getBarchartCommand[key].forEach(id => {
            params = params.append(key, id.toString())
          })
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