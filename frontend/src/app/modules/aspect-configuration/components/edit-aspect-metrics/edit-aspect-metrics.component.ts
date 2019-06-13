import {Component, OnInit, ViewChild} from '@angular/core';
import {Observable, ReplaySubject} from "rxjs";
import {Application} from "../../../../models/application.model";
import {Page} from "../../../../models/page.model";
import {ApplicationService} from "../../../../services/application.service";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {AspectConfigurationService} from "../../services/aspect-configuration.service";
import {ExtendedPerformanceAspect, PerformanceAspectType} from "../../../../models/perfomance-aspect.model";
import {distinctUntilChanged, withLatestFrom} from "rxjs/operators";
import {MetricFinderService} from "../../../metric-finder/services/metric-finder.service";
import {MetricFinderComponent} from "../../../metric-finder/metric-finder.component";
import {AspectMetricsComponent} from "../aspect-metrics/aspect-metrics.component";

@Component({
  selector: 'osm-edit-aspect-metrics',
  templateUrl: './edit-aspect-metrics.component.html',
  styleUrls: ['./edit-aspect-metrics.component.scss']
})
export class EditAspectMetricsComponent implements OnInit {

  @ViewChild(MetricFinderComponent)
  metricFinderCmp: MetricFinderComponent;

  @ViewChild(AspectMetricsComponent)
  aspectMetricsCmp: AspectMetricsComponent;

  application$: Observable<Application>;
  page$: Observable<Page>;
  browserId$ = new ReplaySubject<number>(1);

  performanceAspects$: Observable<ExtendedPerformanceAspect[]>;
  aspectType$: Observable<PerformanceAspectType>;

  constructor(
    private route: ActivatedRoute,
    private applicationService: ApplicationService,
    private aspectConfService: AspectConfigurationService,
    private metricFinderService: MetricFinderService) {

    this.application$ = applicationService.selectedApplication$;
    this.page$ = aspectConfService.selectedPage$;
    this.performanceAspects$ = aspectConfService.extendedAspects$;
    this.aspectType$ = aspectConfService.selectedAspectType$;
    this.initMetricFinderDataLoading();
  }

  ngOnInit() {
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.aspectConfService.loadApplication(params.get('applicationId'));
      this.aspectConfService.loadPage(params.get('pageId'));
      this.browserId$.next(Number(params.get('browserId')));
      this.aspectConfService.initSelectedAspectType(params.get('aspectType'));
      this.loadChartData(Number(params.get('applicationId')), Number(params.get('pageId')), Number(params.get('browserId')));
    });
  }

  private initMetricFinderDataLoading() {
    this.browserId$.pipe(
      distinctUntilChanged(),
      withLatestFrom(this.application$, this.page$)
    ).subscribe(([browserId, app, page]: [number, Application, Page]) => {
      this.loadChartData(app.id, page.id, browserId);
      this.metricFinderCmp.clearResults();
    })
  }

  private loadChartData(appId: number, pageId: number, browserId: number) {
    const now = new Date();
    const from = new Date(now.getTime() - 1000 * 60 * 60 * 24 * 7);
    this.metricFinderService.loadData(from, now, appId, pageId, browserId);
  }

  persistAspect() {
    const perfAspectToCreateOrUpdate = {
      ...this.aspectMetricsCmp.getSelectedAspect(),
      measurand: {name: this.metricFinderCmp.selectedMetric, id: this.metricFinderCmp.selectedMetric}
    };
    this.aspectConfService.createOrUpdatePerformanceAspect(perfAspectToCreateOrUpdate);
  }

}