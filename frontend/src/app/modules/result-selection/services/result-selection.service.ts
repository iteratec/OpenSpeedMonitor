import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {EMPTY, Observable, OperatorFunction} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {URL} from '../../../enums/url.enum';
import {Caller, ResultSelectionCommand} from '../models/result-selection-command.model';
import {MeasurandGroup} from '../../../models/measurand.model';

@Injectable()
export class ResultSelectionService {
  constructor(private http: HttpClient) {
  }

  fetchMeasurands(): Observable<MeasurandGroup[]> {
    return this.http.get<MeasurandGroup[]>(URL.MEASURANDS).pipe(
      this.handleError()
    );
  }

  fetchResultSelectionData<T>(url: URL, resultSelectionCommand: ResultSelectionCommand): Observable<T> {
    if (resultSelectionCommand.from && resultSelectionCommand.to) {
      const params = this.createParamsFromResultSelectionCommand(resultSelectionCommand);
      return this.http.get<T>(url, {params: params}).pipe(
        this.handleError()
      );
    }
    return new Observable<T>();
  }

  private createParamsFromResultSelectionCommand(resultSelectionCommand: ResultSelectionCommand) {
    let params = new HttpParams()
      .set('from', resultSelectionCommand.from.toISOString())
      .set('to', resultSelectionCommand.to.toISOString())
      .set('caller', Caller[resultSelectionCommand.caller]);

    Object.keys(resultSelectionCommand).forEach(key => {
      if (key === 'from' || key === 'to' || key === 'caller') {
        return;
      }
      if (resultSelectionCommand[key].length > 0) {
        resultSelectionCommand[key].forEach(id => {
          params = params.append(key, id.toString());
        });
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
