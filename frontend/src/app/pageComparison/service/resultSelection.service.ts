import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UrlStore } from '../../common/app.url-store'
@Injectable({
  providedIn: 'root'
})
export class ResultSelectionService {

  constructor(private http: HttpClient) {
  }

  getJobGroupToPagesMap(from:string,to:string) {
    from = '2017-08-26T22:00:00.000Z';
    to = '2018-04-30T10:00:00.000Z';
    return this.http.get(UrlStore.GET_JOB_GROUP_TO_PAGES_MAP_URL, { params: {from:from, to:to} });
  }
}
