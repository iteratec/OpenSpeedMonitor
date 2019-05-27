import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TestResult} from '../../models/test-result.model';
import {MetricFinderService} from '../../services/metric-finder.service';

interface SelectableMetric {
  id: string;
  name: string;
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
    return ['SPEED_INDEX', '_HERO_IMAGE', 'START_RENDER'].map(id => ({
      id, name: this.metricFinderService.getMetricName(id)
    }));
  }

}
