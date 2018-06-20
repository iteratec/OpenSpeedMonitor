import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ReplaySubject} from "rxjs/index";
import {IJobGroupToPagesMapping} from "../../../common/model/job-group-to-page-mapping.model";

@Injectable({
  providedIn: 'root'
})
export class PageService {

  public pages$ = new ReplaySubject<IJobGroupToPagesMapping[]>(1);


  constructor(private http: HttpClient) {
    this.updatePages()
  }


  updatePages() {
    this.http.get<IJobGroupToPagesMapping[]>("page/getPagesForActiveJobGroups")
      .subscribe(next => this.pages$.next(next), error => this.handleError(error));
  }

  handleError(error: any){
    console.log(error);
  }
}
