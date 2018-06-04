import {Component, Input, OnInit} from '@angular/core';
import {JobGroup} from "../../model/job-group.model";
import {IPage} from "../../model/page.model";
import {PageService} from "../../service/rest/page.service";
import {filter, map} from "rxjs/internal/operators";
import {Observable} from "rxjs/index";

@Component({
  selector: 'app-page-list',
  templateUrl: './page-list.component.html',
  styleUrls: ['./page-list.component.css']
})
export class PageListComponent {
  pageList$: Observable<IPage[]>;

  @Input() jobGroup: JobGroup;

  constructor(private pageService: PageService) {
    this.pageList$ = this.pageService.pages$.pipe(
      filter(() => !!this.jobGroup),
      map((pages: IPage[]) => this.filterPagesByJobGroup(this.jobGroup, pages))
    );
  }


  private filterPagesByJobGroup(jobGroup: JobGroup, pages: IPage[]): IPage[] {
    return pages
      .filter( a => a.jobGroupId == jobGroup.getId())
  }
}
