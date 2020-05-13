import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {EMPTY, Observable, OperatorFunction} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {GetBarchartCommand} from '../models/get-barchart-command.model';
import {ResultSelectionCommand} from '../../result-selection/models/result-selection-command.model';
import {RemainingResultSelection} from '../../result-selection/models/remaing-result-selection.model';

@Injectable()
export class BarchartDataService {

  constructor(private http: HttpClient) {
  }

  private static buildGetBarchartCommand(
    resultSelectionCommand: ResultSelectionCommand,
    remainingResultSelection: RemainingResultSelection,
    aggregationValue: (string | number)
  ): GetBarchartCommand {
    return new GetBarchartCommand({
      from: resultSelectionCommand.from,
      to: resultSelectionCommand.to,
      fromComparative: remainingResultSelection.fromComparative,
      toComparative: remainingResultSelection.toComparative,
      pages: resultSelectionCommand.pageIds,
      jobGroups: resultSelectionCommand.jobGroupIds,
      measurands: remainingResultSelection.measurands,
      performanceAspectTypes: remainingResultSelection.performanceAspectTypes,
      browsers: resultSelectionCommand.browserIds,
      deviceTypes: remainingResultSelection.deviceTypes,
      operatingSystems: remainingResultSelection.operatingSystems,
      aggregationValue: aggregationValue
    });
  }

  fetchBarchartData<T>(
    resultSelectionCommand: ResultSelectionCommand,
    remainingResultSelection: RemainingResultSelection,
    aggregationValue: (string | number),
    url: string
  ): Observable<T> {
    const getBarchartCommand = BarchartDataService.buildGetBarchartCommand(
      resultSelectionCommand,
      remainingResultSelection,
      aggregationValue
    );
    const params = this.createParams(getBarchartCommand);
    return this.http.get<T>(url, {params: params}).pipe(
      this.handleError()
    );
  }

  private createParams(getBarchartCommand: GetBarchartCommand) {
    let params = new HttpParams();

    Object.keys(getBarchartCommand).forEach(key => {
      if (getBarchartCommand[key]) {
        if (key === 'from' || key === 'to' || key === 'fromComparative' || key === 'toComparative') {
          params = params.append(key, getBarchartCommand[key].toISOString());
        } else if (key === 'aggregationValue') {
          params = params.append(key, getBarchartCommand[key].toString());
        } else if (getBarchartCommand[key].length > 0) {
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
