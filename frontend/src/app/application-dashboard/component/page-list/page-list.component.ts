import {Component, Input, OnChanges} from '@angular/core';
import {ApplicationDashboardService} from "../../service/application-dashboard.service";
import {Observable} from "rxjs/index";
import {PageDto} from "../../model/page.model";

@Component({
  selector: 'osm-page-list',
  templateUrl: './page-list.component.html',
  styleUrls: ['./page-list.component.css']
})
export class PageListComponent implements OnChanges {
  @Input() applicationId: number;
  pages$: Observable<PageDto[]>;

  constructor(private applicationDashboardService: ApplicationDashboardService) {
  }

  ngOnChanges() {
    this.pages$ = this.applicationDashboardService.getPagesForJobGroup(this.applicationId);
  }

}
