import {Component} from '@angular/core';
import {ApplicationDashboardService} from "../../services/application-dashboard.service";
import {Observable} from "rxjs/index";
import {MetricsDto} from "../../models/metrics.model";

@Component({
  selector: 'osm-page-list',
  templateUrl: './page-list.component.html',
  styleUrls: ['./page-list.component.scss']
})
export class PageListComponent {
  metrics$: Observable<MetricsDto[]>;

  constructor(private applicationDashboardService: ApplicationDashboardService) {
    this.metrics$ = this.applicationDashboardService.metrics$;
  }
}
