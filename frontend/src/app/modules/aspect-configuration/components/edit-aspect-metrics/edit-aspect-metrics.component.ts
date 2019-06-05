import {Component, OnInit, ViewChild} from '@angular/core';
import {Observable, ReplaySubject} from "rxjs";
import {Application} from "../../../../models/application.model";
import {Page} from "../../../../models/page.model";
import {ApplicationService} from "../../../../services/application.service";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {AspectConfigurationService} from "../../services/aspect-configuration.service";
import {ExtendedPerformanceAspect, PerformanceAspectType} from "../../../../models/perfomance-aspect.model";
import {distinctUntilChanged, filter, map, withLatestFrom} from "rxjs/operators";
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
  performanceAspects$: Observable<ExtendedPerformanceAspect[]>;
  aspectType$: Observable<PerformanceAspectType>;
  browserId$ = new ReplaySubject<number>(1);

  constructor(
    private route: ActivatedRoute,
    private applicationService: ApplicationService,
    private aspectConfService: AspectConfigurationService,
    private metricFinderService: MetricFinderService) {

    this.application$ = applicationService.selectedApplication$;
    this.page$ = aspectConfService.selectedPage$;
    this.performanceAspects$ = aspectConfService.extendedAspects$;
    this.initMetricFinderDataLoading();

  }

  ngOnInit() {
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.initAspectType(params);
      this.browserId$.next(Number(params.get('browserId')));
      this.aspectConfService.loadApplication(params.get('applicationId'));
      this.aspectConfService.loadPage(params.get('pageId'));
    });
  }

  private initAspectType(params: ParamMap) {
    this.aspectType$ = this.aspectConfService.uniqueAspectTypes$.pipe(
      filter((types: PerformanceAspectType[]) => types.length > 0),
      map((types: PerformanceAspectType[]) => {
        return types.find((type: PerformanceAspectType) => type.name == params.get('aspectType'))
      })
    );
    this.aspectType$.subscribe((type: PerformanceAspectType) => {
      console.log(`type=${JSON.stringify(type)}`)
    });
  }

  private initMetricFinderDataLoading() {
    this.browserId$.pipe(
      distinctUntilChanged(),
      withLatestFrom(this.application$, this.page$)
    ).subscribe(([browserId, app, page]: [number, Application, Page]) => {
      const now = new Date();
      const from = new Date(now.getTime() - 1000 * 60 * 60 * 24 * 7);
      console.log(`from=${from}|to=${now}|app=${app.id}|page=${page.id}|browser=${browserId}`);
      this.metricFinderService.loadData(from, now, app.id, page.id, browserId);
    })
  }

  persistAspect() {
    const perfAspectToCreateOrUpdate = {
      ...this.aspectMetricsCmp.getSelectedAspect(),
      measurand: {name: this.metricFinderCmp.selectedMetric, id: this.metricFinderCmp.selectedMetric}
    };
    this.aspectConfService.createOrUpdatePerformanceAspect(perfAspectToCreateOrUpdate);
  }

}
