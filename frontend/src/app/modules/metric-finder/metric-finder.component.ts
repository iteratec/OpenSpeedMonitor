import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {TestResult} from './models/test-result';
import {MetricFinderService} from './services/metric-finder.service';

@Component({
  selector: 'osm-metric-finder',
  templateUrl: './metric-finder.component.html',
  styleUrls: ['./metric-finder.component.scss']
})
export class MetricFinderComponent {
  public testResults$: Observable<TestResult[]>;
  public selected: TestResult[] = [];
  public metric = 'speedIndex';

  constructor(private metricFinderService: MetricFinderService) {
    this.testResults$ = metricFinderService.testResults$;
    metricFinderService.loadTestData();
  }

}
