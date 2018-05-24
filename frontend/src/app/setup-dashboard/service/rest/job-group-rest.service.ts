import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Configuration } from '../../../common/app.url-store'

@Injectable({
  providedIn: 'root'
})
export class JobGroupRestService {

  constructor(private http: HttpClient, private _configuration: Configuration) {
  }

  getActiveJobGroups() {
    return this.http.get(this._configuration.getActiveJobUrl)
  }
}
