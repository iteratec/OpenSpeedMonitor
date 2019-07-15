import {Component, OnInit, ViewChild} from '@angular/core';
import {combineLatest, Observable, ReplaySubject} from "rxjs";
import {Application} from "../../../../models/application.model";
import {Page} from "../../../../models/page.model";
import {ApplicationService} from "../../../../services/application.service";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {AspectConfigurationService} from "../../services/aspect-configuration.service";
import {
  ExtendedPerformanceAspect,
  PerformanceAspect,
  PerformanceAspectType
} from "../../../../models/perfomance-aspect.model";
import {distinctUntilChanged, map, withLatestFrom} from "rxjs/operators";
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
  aspectsToShow$: Observable<ExtendedPerformanceAspect[]>;
  selectedMeasurand$: Observable<any>;
  private selectedAspect: ExtendedPerformanceAspect;
  private selectedMeasurand: string;
  selectedMeasurand$: Observable<any>;

  constructor(
    private route: ActivatedRoute,
    private applicationService: ApplicationService,
    private aspectConfService: AspectConfigurationService,
    private metricFinderService: MetricFinderService ) {

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

      const browserId = Number(params.get('browserId'));
      this.browserId$.next(browserId);
      this.aspectConfService.initSelectedAspectType(params.get('aspectType'));
      this.loadChartData(Number(params.get('applicationId')), Number(params.get('pageId')), Number(params.get('browserId')));

      combineLatest(this.aspectConfService.extendedAspects$, this.aspectConfService.selectedAspectType$).subscribe(
        ([aspects, selectedType]: [ExtendedPerformanceAspect[], PerformanceAspectType]) => {

          console.log(
            "aspects: " + JSON.stringify (aspects, null, 4) +
            "selectedType: " + JSON.stringify (selectedType, null, 4)
          );

          //aspects.filter((aspect: ExtendedPerformanceAspect) => aspect.performanceAspectType.name == selectedType.name);

          this.selectedAspect = aspects
            .filter((aspect: ExtendedPerformanceAspect) => aspect.performanceAspectType.name == selectedType.name)
            .find((aspect: ExtendedPerformanceAspect) => {
              return aspect.browserId === browserId;


          });

          console.log("selectedAspect: ", JSON.stringify (this.selectedAspect, null, 4));
          if (typeof this.selectedAspect != "undefined") {

            this.selectedMeasurand= this.selectedAspect.measurand.name;
            console.log("selectedMeasurand: " + this.selectedMeasurand);
          }
          console.log("this.browserId: " + browserId);
        }
      );
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

//[selectedAspect]="selectedAspect"
