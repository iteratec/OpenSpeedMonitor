import {Component, Input} from '@angular/core';
import {IPage} from "../../model/page.model";
import {Observable} from "rxjs/internal/Observable";
import {MetricsDto} from "../../model/metrics.model";
import {ApplicationDashboardService} from "../../service/application-dashboard.service";
import {map} from "rxjs/operators";

@Component({
  selector: 'osm-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.css']
})
export class PageComponent {
  @Input() page: IPage;
  metricsForPage$: Observable<MetricsDto>;

  constructor(private applicationDashboardService: ApplicationDashboardService) {
    this.metricsForPage$ = applicationDashboardService.metrics$.pipe(
      map((next: MetricsDto[]) => next.find((metricsDto: MetricsDto) => metricsDto.pageId == this.page.id)));
  }

  private round(value: number): number {
    const multiplier = Math.pow(10, 1);
    return Math.round(value * multiplier) / multiplier;
  }

  convertByteToMiB(bytes: number): number {
    return bytes / 1048576;
  }

  transform(value: number): string {
    return this.round(value).toFixed(2);
  }
}
