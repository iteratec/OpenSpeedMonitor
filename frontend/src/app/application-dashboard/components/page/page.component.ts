import {Component, Input} from '@angular/core';
import {Observable} from "rxjs/internal/Observable";
import {PageMetricsDto} from "../../models/page-metrics.model";
import {ApplicationDashboardService} from "../../services/application-dashboard.service";
import {map} from "rxjs/operators";
import {CalculationUtil} from "../../../shared/utils/calculation.util";
import {PageCsiDto} from "../../models/page-csi.model";
import {ResponseWithLoadingState} from "../../models/response-with-loading-state.model";

@Component({
  selector: 'osm-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.scss']
})
export class PageComponent {
  @Input() lastDateOfResult: string;
  @Input() metricsForPage: PageMetricsDto;
  pageCsi$: Observable<number>;
  pageCsiDate$: Observable<string>;
  isLoading: boolean = true;

  constructor(private applicationDashboardService: ApplicationDashboardService) {
    this.pageCsi$ = applicationDashboardService.pageCsis$.pipe(
      map((next: ResponseWithLoadingState<PageCsiDto[]>) => {
        this.isLoading = next.isLoading;
        if (this.isLoading) return 0;
        const pageCsiDto: PageCsiDto = next.data.find((pageCsiDto: PageCsiDto) => pageCsiDto.pageId == this.metricsForPage.pageId);
        return pageCsiDto ? pageCsiDto.csiDocComplete : null;
      })
    );

    this.pageCsiDate$ = applicationDashboardService.pageCsis$.pipe(
      map((next: ResponseWithLoadingState<PageCsiDto[]>) => {
        this.isLoading = next.isLoading;
        if (this.isLoading) return null;
        const pageCsiDto: PageCsiDto = next.data.find((pageCsiDto: PageCsiDto) => pageCsiDto.pageId == this.metricsForPage.pageId);
        return pageCsiDto ? pageCsiDto.date : null;
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
