import {Component, Input, OnInit} from '@angular/core';
import {JobGroup} from "../../model/job-group.model";
import {Page, PageFromJson} from "../../model/page.model";
import {PageService} from "../../service/rest/page.service";

@Component({
  selector: 'app-page-list',
  templateUrl: './page-list.component.html',
  styleUrls: ['./page-list.component.css']
})
export class PageListComponent implements OnInit {
  @Input() jobGroup: JobGroup;
  pageList: Page[];

  constructor(private pageService: PageService) { }

  ngOnInit() {
    this.getPagesForJobGroup(this.jobGroup)
  }

  getPagesForJobGroup(jobgroup: JobGroup){
    this.pageService.getPagesFor(jobgroup).subscribe( (response: any[]) => {
      this.pageList = response.map(pageJson => new PageFromJson(pageJson))
    }, error => {
      console.log(error)
    })
  }
}
