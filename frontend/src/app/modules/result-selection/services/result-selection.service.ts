import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {EMPTY, Observable, OperatorFunction, ReplaySubject} from "rxjs";
import {catchError} from "rxjs/operators";
import {URL} from "../../../enums/url.enum";
import {Application} from "../../../models/application.model";
import {Page} from "../../../models/page.model";
import {Caller, ResultSelectionCommand} from "../models/result-selection-command.model";

@Injectable()
export class ResultSelectionService {
  selectedApplications$: ReplaySubject<Application[]> = new ReplaySubject<Application[]>(1);
  selectedPages$: ReplaySubject<Page[]> = new ReplaySubject<Page[]>(1);

  constructor(private http: HttpClient) {
  }

  updateApplications(applications: Application[]) {
    this.selectedApplications$.next(applications);
    this.selectedPages$.next([]);
  }

  fetchResultSelectionData<T>(resultSelectionCommand: ResultSelectionCommand, url: URL): Observable<T> {
    const params = this.createParamsFromResultSelectionCommand(resultSelectionCommand);
    return this.http.get<T>(url, {params: params}).pipe(
      this.handleError()
    )
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
      resultSelectionCommand[key].forEach(id => {
        params = params.append(key, id.toString())
      })
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
