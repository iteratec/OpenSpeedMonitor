import {BehaviorSubject} from 'rxjs';
import {TestResult} from '../models/test-result.model';

export class MetricFinderServiceMock {
  public testResults$ = new BehaviorSubject<TestResult[]>([]);

  loadTestData(): void {

  }

  public loadData(from: Date, to: Date, application: number, page: number, browser: number): void {

  }


  getMetricName(metric: string): string {
    return 'name:' + metric;
  }
}
