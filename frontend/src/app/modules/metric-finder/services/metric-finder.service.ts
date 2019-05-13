import { Injectable } from '@angular/core';
import {TestInfo, TestResult} from '../models/test-result';
import {BehaviorSubject} from 'rxjs';

@Injectable()
export class MetricFinderService {
  public testResults$ = new BehaviorSubject<TestResult[]>([]);

  constructor() {
    this.mockData();
  }

  private mockData() {
    const testInfo = new TestInfo('XY_Z0_20190512', 1, false, 1, 'https://webpagetest.org')
    const now = Date.now();
    const hourInMillisecs = 1000 * 60 * 60;
    this.testResults$.next([
      new TestResult(new Date(now - 5 * hourInMillisecs), testInfo, { 'SPEED_INDEX': 2000}),
      new TestResult(new Date(now - 4 * hourInMillisecs), testInfo, { 'SPEED_INDEX': 3000}),
      new TestResult(new Date(now - 3 * hourInMillisecs), testInfo, { 'SPEED_INDEX': 2400}),
      new TestResult(new Date(now - 2 * hourInMillisecs), testInfo, { 'SPEED_INDEX': 1987}),
      new TestResult(new Date(now - hourInMillisecs), testInfo, { 'SPEED_INDEX': 1698})
    ]);
  }

}
