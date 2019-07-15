import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TestResult} from '../../models/test-result.model';
import {MetricFinderService} from '../../services/metric-finder.service';

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
export class MetricSelectionComponent {

  private _selectedMetric = '';
  private _availableMetrics: SelectableMetric[] = [];

  @Input()
  set results(results: TestResult[]) {
    this._availableMetrics = this.findCommonMetrics(results);
  }

  @Input()
  get selectedMetric() {
    return this._selectedMetric;
  }

  set selectedMetric(metric: string) {
    this._selectedMetric = metric;
    this.selectedMetricChange.emit(this._selectedMetric);
  }

  @Output()
  selectedMetricChange = new EventEmitter<string>();

  get availableMetrics(): SelectableMetric[] {
    return this._availableMetrics;
  }

  constructor(
    private metricFinderService: MetricFinderService
  ) {
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
