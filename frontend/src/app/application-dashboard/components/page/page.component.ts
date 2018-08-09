import {Component, Input} from '@angular/core';
import {PageDto} from "../../models/page.model";
import {Observable} from "rxjs/internal/Observable";
import {MetricsDto} from "../../models/metrics.model";
import {ApplicationDashboardService} from "../../services/application-dashboard.service";
import {map} from "rxjs/operators";
import {CalculationUtil} from "../../../shared/utils/calculation.util";
import {PageCsiDto} from "../../models/page-csi.model";

@Component({
  selector: 'osm-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.scss']
})
export class PageComponent {
  @Input() page: PageDto;
  metricsForPage$: Observable<MetricsDto>;
  pageCsi$: Observable<number>;
  isLoading: boolean = true;

  constructor(private applicationDashboardService: ApplicationDashboardService) {
    this.metricsForPage$ = applicationDashboardService.metrics$.pipe(
      map((next: MetricsDto[]) => next.find((metricsDto: MetricsDto) => metricsDto.pageId == this.page.id)));

    this.pageCsi$ = applicationDashboardService.pageCsis$.pipe(
      map((next: any) => {
        this.isLoading = next.isLoading;
        if (this.isLoading) return 0;
        const pageCsiDto: PageCsiDto = next.pageCsis.find((pageCsiDto: PageCsiDto) => pageCsiDto.pageId == this.page.id);
        return pageCsiDto ? pageCsiDto.csiDocComplete : null;
      })
    );
  }

  transform(value: number): string {
    return CalculationUtil.toRoundedStringWithFixedDecimals(value, 2);
  }

  convertToMib(value: number): number {
    return CalculationUtil.convertBytesToMiB(value);
  }

  convertMillisecsToSecs(value: number): number {
    return CalculationUtil.convertMillisecsToSecs(value);
  }
}
