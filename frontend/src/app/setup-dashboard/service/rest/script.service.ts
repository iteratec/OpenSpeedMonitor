import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ReplaySubject} from "rxjs/internal/ReplaySubject";
import {ScriptDto} from "../../model/script.model";

@Injectable({
  providedIn: 'root'
})
export class ScriptService {

  public scripts$ = new ReplaySubject<ScriptDto[]>(1);

  constructor(private http: HttpClient) {
    this.updateScripts()
  }

  updateScripts() {
    this.http.get<ScriptDto[]>('/script/getScriptsForActiveJobGroups')
      .subscribe(next => this.scripts$.next(next), error => this.handleError(error));
  }

  handleError(error: any) {
    console.log(error);
  }
}
