import { Injectable } from '@angular/core';
import {TestResult, TestResultDTO} from '../models/test-result';
import {BehaviorSubject} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {map} from 'rxjs/operators';

@Injectable()
export class MetricFinderService {
  public testResults$ = new BehaviorSubject<TestResult[]>([]);

  constructor(
    private http: HttpClient
  ) {
  }

  public loadTestData() {
    const now = Date.now();
    const dayInMillisecs = 1000 * 60 * 60 * 24;
    this.loadData(new Date(now - 28 * dayInMillisecs), new Date(now), 94, 76, 4);
  }

  public loadData(from: Date, to: Date, application: number, page: number, browser: number) {
    const params = {
      from: from.toISOString(),
      to: to.toISOString(),
      applicationId: application.toString(),
      pageId: page.toString(),
      browserId: browser.toString()
    };
    this.http.get<TestResultDTO[]>('/metricFinder/rest/getEventResults', {params}).pipe(
      map(dtos => dtos.map(dto => new TestResult(dto)))
    ).subscribe(next => this.testResults$.next(next));
  }

}
