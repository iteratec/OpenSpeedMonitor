import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {TestResult} from './models/test-result.model';
import {MetricFinderService} from './services/metric-finder.service';

@Component({
  selector: 'osm-metric-finder',
  templateUrl: './metric-finder.component.html',
  styleUrls: ['./metric-finder.component.scss']
})
export class MetricFinderComponent {
  public testResults$: Observable<TestResult[]>;
  public selectedResults: TestResult[] = [];
  public selectedMetric = 'SPEED_INDEX';

  constructor(private metricFinderService: MetricFinderService) {
    this.testResults$ = metricFinderService.testResults$;
  }

  setSelectedResults(results: TestResult[]) {
    this.selectedResults = [...results].sort((a, b) => a.date.getTime() - b.date.getTime());
  }
}
