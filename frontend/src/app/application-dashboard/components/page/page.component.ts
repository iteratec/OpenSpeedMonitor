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
  @Input() lastDateOfResult: string;
  metricsForPage$: Observable<MetricsDto>;
  pageCsi$: Observable<number>;
  pageCsiDate$: Observable<string>;

  constructor(private applicationDashboardService: ApplicationDashboardService) {
    this.metricsForPage$ = applicationDashboardService.metrics$.pipe(
      map((next: MetricsDto[]) => next.find((metricsDto: MetricsDto) => metricsDto.pageId == this.page.id)));

    this.pageCsi$ = applicationDashboardService.pageCsis$.pipe(
      map((next: PageCsiDto[]) => next.find((pageCsiDto: PageCsiDto) => pageCsiDto.pageId == this.page.id)),
      map((pageCsiDto: PageCsiDto) => pageCsiDto ? pageCsiDto.csiDocComplete : null)
    );

    this.pageCsiDate$ = applicationDashboardService.pageCsis$.pipe(
      map((next: PageCsiDto[]) => next.find((pageCsiDto: PageCsiDto) => pageCsiDto.pageId == this.page.id)),
      map((pageCsiDto: PageCsiDto) => pageCsiDto ? pageCsiDto.date : null)
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

  generateToolTip(lastDate: string): string {
    console.log(this.lastDateOfResult)
    if (lastDate < new Date(this.lastDateOfResult).toISOString().substring(0, 10)) {
      return "This value is from " + new Date(lastDate).toLocaleDateString("de-DE") + ",\nas there are no measurements today.";
    }
    return "";
  }
}
