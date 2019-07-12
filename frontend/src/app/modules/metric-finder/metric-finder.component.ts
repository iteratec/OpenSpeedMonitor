import {Component, Input, ViewChild} from '@angular/core';
import {Observable} from 'rxjs';
import {TestResult} from './models/test-result.model';
import {MetricFinderService} from './services/metric-finder.service';
import {LineChartComponent} from "./components/line-chart/line-chart.component";
import {ComparableFilmstripsComponent} from "./components/comparable-filmstrips/comparable-filmstrips.component";
import {PerformanceAspect} from "../../models/perfomance-aspect.model";

@Component({
  selector: 'osm-metric-finder',
  templateUrl: './metric-finder.component.html',
  styleUrls: ['./metric-finder.component.scss']
})
export class MetricFinderComponent {

  // @Input() selectedAspect: PerformanceAspect;

  @ViewChild(LineChartComponent)
  lineChartCmp: LineChartComponent;

  @ViewChild(ComparableFilmstripsComponent)
  compFilmstripCmp: ComparableFilmstripsComponent;

  public testResults$: Observable<TestResult[]>;
  public selectedResults: TestResult[] = [];
  public selectedMetric = 'SPEED_INDEX';

  constructor(private metricFinderService: MetricFinderService) {
    this.testResults$ = metricFinderService.testResults$;
  }

  setSelectedResults(results: TestResult[]) {
    this.selectedResults = [...results].sort((a, b) => a.date.getTime() - b.date.getTime());
  }

  clearResults() {
    this.lineChartCmp.clearSelection();
    this.lineChartCmp.clearResults();
    this.lineChartCmp.redraw();
    this.compFilmstripCmp.clearResults();
  }
}
