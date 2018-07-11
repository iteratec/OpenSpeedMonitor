import {Component, Input, OnChanges} from '@angular/core';
import {ApplicationDashboardService} from "../../services/application-dashboard.service";
import {Observable} from "rxjs/index";
import {PageDto} from "../../models/page.model";

@Component({
  selector: 'osm-page-list',
  templateUrl: './page-list.component.html',
  styleUrls: ['./page-list.component.scss']
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
