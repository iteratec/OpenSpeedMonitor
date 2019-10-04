import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {EMPTY, Observable, OperatorFunction} from "rxjs";
import {catchError} from "rxjs/operators";
import {URL} from "../../../enums/url.enum";
import {Caller, ResultSelectionCommand} from "../models/result-selection-command.model";

@Injectable()
export class ResultSelectionService {
  constructor(private http: HttpClient) {
  }

  fetchResultSelectionData<T>(resultSelectionCommand: ResultSelectionCommand, url: URL): Observable<T> {
    const params = this.createParamsFromResultSelectionCommand(resultSelectionCommand);
    return this.http.get<T>(url, {params: params}).pipe(
      this.handleError()
    )
  }

  private createParamsFromResultSelectionCommand(resultSelectionCommand: ResultSelectionCommand) {
    let params = new HttpParams();

    Object.keys(resultSelectionCommand).forEach(key => {
      if (resultSelectionCommand[key].length > 0) {
        if (key === 'from' || key === 'to') {
          params.append(key, resultSelectionCommand[key].toISOString())
        } else if (key === 'caller') {
          params.append(key, Caller[resultSelectionCommand[key]])
        } else {
          resultSelectionCommand[key].forEach(id => {
            params = params.append(key, id.toString())
          });
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
