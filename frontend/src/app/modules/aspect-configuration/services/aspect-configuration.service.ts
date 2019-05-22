import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Application, ApplicationDTO} from "../../../models/application.model";
import {catchError, map} from "rxjs/operators";
import {BehaviorSubject, EMPTY} from "rxjs";
import {Page} from "../../../models/page.model";
import {BrowserInfoDto} from "../../../models/browser.model";

@Injectable({
  providedIn: 'root'
})
export class AspectConfigurationService {

  browserInfos$ = new BehaviorSubject<BrowserInfoDto[]>([]);

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
      map(dto => new Application(dto))
    )
  }

  loadPage(pageId: string) {
    return this.http.get<Page>(
      '/aspectConfiguration/rest/getPage',
      {params: {pageId: pageId}}).pipe(
      catchError((error) => {
        console.error(error);
        return EMPTY;
      })
    )
  }

  loadBrowserInfos() {
    this.http.get<BrowserInfoDto[]>(
      '/aspectConfiguration/rest/getBrowserInformations').pipe(
      catchError((error) => {
        console.error(error);
        return EMPTY;
      })
    ).subscribe(nextBrowserInfo => this.browserInfos$.next(nextBrowserInfo))
  }
}
