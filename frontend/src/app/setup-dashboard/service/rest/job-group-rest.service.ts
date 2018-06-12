import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UrlStore } from '../../../common/app.url-store'

@Injectable({
  providedIn: 'root'
})
export class JobGroupRestService {

  constructor(private http: HttpClient) {
  }

  getActiveJobGroups() {
    return this.http.get(UrlStore.GET_ACTIVE_JOB_URL)
  }
}
