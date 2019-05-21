import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Application, ApplicationDTO} from "../../../models/application.model";
import {catchError, map} from "rxjs/operators";
import {EMPTY} from "rxjs";
import {Loading} from "../../../models/Loading";
import {Page} from "../../../models/page.model";
import {BrowserInfoDto} from "../../../models/browser.model";

@Injectable({
  providedIn: 'root'
})
export class AspectConfigurationService {

  constructor(private http: HttpClient) {
  }

  loadApplication(applicationId: string) {
    return this.http.get<ApplicationDTO>(
      "/applicationDashboard/rest/getApplication",
      {params: {applicationId: applicationId}}).pipe(
      catchError((error) => {
        console.error(error);
        return EMPTY;
      }),
      map(dto => new Application(dto)),
      map(application => ({
        ...application,
        isLoading: false
      }))
    )
  }

  loadPage(pageId: string) {
    return this.http.get<Page & Loading[]>(
      '/aspectConfiguration/rest/getPage',
      {params: {pageId: pageId}}).pipe(
      catchError((error) => {
        console.error(error);
        return EMPTY;
      }),
      map(page => ({
        ...page,
        isLoading: false
      }))
    )
  }

  loadBrowserInfos() {
    return this.http.get<BrowserInfoDto[]>(
      '/aspectConfiguration/rest/getBrowserInformations').pipe(
      catchError((error) => {
        console.error(error);
        return EMPTY;
      })
    )
  }
}
