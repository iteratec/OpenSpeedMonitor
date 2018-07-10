import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ReplaySubject} from "rxjs/index";
import {JobGroupToPagesMappingDto} from "../../shared/model/job-group-to-page-mapping.model";

@Injectable({
  providedIn: 'root'
})
export class PageService {

  public pages$ = new ReplaySubject<JobGroupToPagesMappingDto[]>(1);


  constructor(private http: HttpClient) {
    this.updatePages()
  }


  updatePages() {
    this.http.get<JobGroupToPagesMappingDto[]>("/page/getPagesForActiveJobGroups")
      .subscribe(next => this.pages$.next(next), error => this.handleError(error));
  }

  handleError(error: any){
    console.log(error);
  }
}
