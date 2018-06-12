import {Component, Input, OnInit} from '@angular/core';
import {JobGroup} from "../../model/job-group.model";
import {IPageId} from "../../../common/model/page.model";
import {PageService} from "../../service/rest/page.service";
import {map} from "rxjs/internal/operators";
import {Observable} from "rxjs/index";
import {IJobGroupToPagesMapping} from "../../../common/model/job-group-to-page-mapping.model";

@Component({
  selector: 'app-page-list',
  templateUrl: './page-list.component.html',
  styleUrls: ['./page-list.component.css']
})
export class PageListComponent {
  pageList$: Observable<IPageId[]>;

  @Input() jobGroup: JobGroup;

  constructor(private pageService: PageService) {
    this.pageList$ = this.pageService.pages$.pipe(
      map((jobGroupsWithPages: IJobGroupToPagesMapping[]) => this.filterPagesByJobGroup(this.jobGroup, jobGroupsWithPages))
    );
  }

  private filterPagesByJobGroup(jobGroup: JobGroup, jobGroupsWithPages: IJobGroupToPagesMapping[]): IPageId[] {
    return jobGroupsWithPages.find( a => a.id == jobGroup.getId()).pages;
  }
}
