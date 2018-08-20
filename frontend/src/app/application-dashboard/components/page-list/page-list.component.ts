import {Component} from '@angular/core';
import {ApplicationDashboardService} from "../../services/application-dashboard.service";
import {Observable} from "rxjs/index";
import {PageMetricsDto} from "../../models/page-metrics.model";

@Component({
  selector: 'osm-page-list',
  templateUrl: './page-list.component.html',
  styleUrls: ['./page-list.component.scss']
})
export class PageListComponent {
  metrics$: Observable<PageMetricsDto[]>;

  constructor(private applicationDashboardService: ApplicationDashboardService) {
    this.metrics$ = this.applicationDashboardService.metrics$;
  }
}
