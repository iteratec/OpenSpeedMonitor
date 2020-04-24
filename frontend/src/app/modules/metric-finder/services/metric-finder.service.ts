import {Injectable} from '@angular/core';
import {TestResult, TestResultDTO} from '../models/test-result.model';
import {BehaviorSubject} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {map} from 'rxjs/operators';
import {TranslateService} from '@ngx-translate/core';

@Injectable()
export class MetricFinderService {
  public testResults$ = new BehaviorSubject<TestResult[]>([]);

  constructor(
    private http: HttpClient,
    private translationService: TranslateService
  ) {
  }

  public loadData(from: Date, to: Date, application: number, page: number, browser: number): void {
    const params = {
      from: from.toISOString(),
      to: to.toISOString(),
      applicationId: application.toString(),
      pageId: page.toString(),
      browserId: browser.toString()
    };
    this.http.get<TestResultDTO[]>('/metricFinder/rest/getEventResults', {params}).pipe(
      map(dtos => dtos.map(dto => new TestResult(dto)))
    ).subscribe(
      next => this.testResults$.next(next),
      _ => this.testResults$.next([])
    );
  }

  public getMetricName(metric: string): string {
    const prefixes = {
      _HERO_: 'Hero ',
      _UTME_: 'User Timing ',
      _UTMK_: 'User Timing Measure '
    };
    const matchingPrefix = Object.keys(prefixes).find(prefix => metric.startsWith(prefix));
    if (matchingPrefix) {
      return prefixes[matchingPrefix] + metric.substr(matchingPrefix.length);
    } else {
      return this.translationService.instant('frontend.de.iteratec.isr.measurand.' + metric);
    }
  }
}
