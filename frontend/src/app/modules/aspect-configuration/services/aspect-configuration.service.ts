import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Application, ApplicationDTO} from "../../../models/application.model";
import {catchError, map, startWith} from "rxjs/operators";
import {BehaviorSubject, EMPTY} from "rxjs";
import {Loading} from "../../../models/Loading";
import {Page} from "../../../models/page.model";

@Injectable({
  providedIn: 'root'
})
export class AspectConfigurationService {

  application$ = new BehaviorSubject<Application & Loading>(null);
  page$ = new BehaviorSubject<Page & Loading>(null)

  constructor(private http: HttpClient) {
  }

  loadApplication(applicationId: string) {
    this.http.get<ApplicationDTO>(
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
      })),
      startWith({
        ...this.application$.getValue(),
        isLoading: true
      })
    ).subscribe((app: Application & Loading) => {this.application$.next(app)})
  }

  loadPage(pageId: string) {
    this.http.get<Page & Loading[]>(
      '/aspectConfiguration/rest/getPage',
      {params: {pageId: pageId}}).pipe(
      catchError((error) => {
        console.error(error);
        return EMPTY;
      }),
      map(page => ({
        ...page,
        isLoading: false
      })),
      startWith({
        ...this.page$.getValue(),
        isLoading: true
      })
    ).subscribe((page: Page & Loading) => this.page$.next(page))
  }
}
