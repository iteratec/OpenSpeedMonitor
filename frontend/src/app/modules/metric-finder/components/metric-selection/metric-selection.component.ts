import {AfterViewInit, Component, EventEmitter, Input, Output} from '@angular/core';
import {TestResult} from '../../models/test-result.model';
import {MetricFinderService} from '../../services/metric-finder.service';
import {ResultSelectionService} from '../../../result-selection/services/result-selection.service';
import {MeasurandGroup, SelectableMeasurand} from '../../../../models/measurand.model';

interface SelectableMetric {
  id: string;
  name: string;
  isUserTiming: boolean;
}

@Component({
  selector: 'osm-metric-selection',
  templateUrl: './metric-selection.component.html',
  styleUrls: ['./metric-selection.component.scss']
})
export class MetricSelectionComponent implements AfterViewInit {

  @Output()
  selectedMetricChange = new EventEmitter<string>();

  constructor(
    private metricFinderService: MetricFinderService,
    private measurandService: ResultSelectionService
  ) {
  }

  private _selectedMetric = '';
  private _availableMetrics: SelectableMetric[] = [];

  @Input()
  get selectedMetric() {
    return this._selectedMetric;
  }

  set selectedMetric(metric: string) {
    this._selectedMetric = metric;
    this.selectedMetricChange.emit(this._selectedMetric);
  }

  get availableMetrics(): SelectableMetric[] {
    return this._availableMetrics;
  }

  @Input()
  set results(results: TestResult[]) {
    this._availableMetrics = this.findCommonMetrics(results);
  }

  ngAfterViewInit() {
    this.findDefaultMetrics();
  }

  private findCommonMetrics(results: TestResult[]): SelectableMetric[] {
    const metricIdLists = results.map(result => Object.keys(result.timings));
    return this.intersect(metricIdLists)
      .map(metricId => ({
        id: metricId,
        name: this.metricFinderService.getMetricName(metricId),
        isUserTiming: metricId.startsWith('_')
      }))
      .sort((a, b) => this.compareMetrics(a, b));
  }

  private findDefaultMetrics(): void {
    this.metricFinderService.testResults$.subscribe((next: TestResult[]) => {
      if (next.length === 0) {
        this.measurandService.fetchMeasurands().subscribe((groups: MeasurandGroup[]) => {
          const metrics: SelectableMetric[] = [];
          groups.forEach((group: MeasurandGroup) => {
            group.values.forEach((measurand: SelectableMeasurand) => {
              metrics.push({
                id: measurand.id,
                name: this.metricFinderService.getMetricName(measurand.id),
                isUserTiming: measurand.id.startsWith('_')
              } as SelectableMetric);
            });
          });
          metrics.sort((a, b) => this.compareMetrics(a, b));
          this._availableMetrics = metrics;
        });
      } else if (next.length > 0) {
        this._availableMetrics = [];
      }
    });
  }

  private intersect(metricLists: string[][]): string[] {
    if (!metricLists || !metricLists.length) {
      return [];
    }
    const otherLists = metricLists.slice(1);
    return metricLists[0].reduce((intersection, metric) => {
      if (!intersection.includes(metric) && otherLists.every(metrics => metrics.includes(metric))) {
        intersection.push(metric);
      }
      return intersection;
    }, []);
  }

  private compareMetrics(a: SelectableMetric, b: SelectableMetric): number {
    if (a.isUserTiming && !b.isUserTiming) {
      return 1;
    }
    if (b.isUserTiming && !a.isUserTiming) {
      return -1;
    }
    return a.id.localeCompare(b.id);
  }

}
