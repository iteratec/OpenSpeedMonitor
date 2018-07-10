import {Component, Input, OnChanges} from '@angular/core';
import {ApplicationDashboardService} from "../../services/application-dashboard.service";
import {Observable} from "rxjs/index";
import {IPage} from "../../models/page.model";

@Component({
  selector: 'osm-page-list',
  templateUrl: './page-list.component.html',
  styleUrls: ['./page-list.component.css']
})
export class PageListComponent implements OnChanges {
  @Input() applicationId: number;
  pages$: Observable<IPage[]>;

  constructor(private applicationDashboardService: ApplicationDashboardService) {
  }

  ngOnChanges() {
    this.pages$ = this.applicationDashboardService.getPagesForJobGroup(this.applicationId);
  }

}
