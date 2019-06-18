import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {EMPTY, Observable, OperatorFunction} from "rxjs";
import {catchError} from "rxjs/operators";
import {GetBarchartCommand} from "../models/get-barchart-command.model";

@Injectable({
  providedIn: 'root'
})
export class AggregationService {

  constructor(private http: HttpClient) { }

  fetchBarChartData<T>(getBarchartCommand: GetBarchartCommand, url: string): Observable<T> {
    const params = this.createParams(getBarchartCommand);
    return this.http.get<T>(url, {params: params}).pipe(
      this.handleError()
    )
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

    console.log(params);
    return params;
  }

  private handleError(): OperatorFunction<any, any> {
    return catchError((error) => {
      console.error(error);
      return EMPTY;
    });
  }
}
