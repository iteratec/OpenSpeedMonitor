import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {JobGroup} from "../../model/job-group.model";
import {Observable} from "rxjs/index";

@Injectable({
  providedIn: 'root'
})
export class PageService {

  constructor(private http: HttpClient) { }

  getPagesFor(jobGroup: JobGroup): Observable<any>{

    let params = new HttpParams().set('jobGroupId', jobGroup.getId().toString());

    return this.http.get("page/getPagesForJobGroup", {params : params})
  }
}
