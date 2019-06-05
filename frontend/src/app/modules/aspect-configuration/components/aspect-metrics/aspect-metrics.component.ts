import {Component, Input, OnInit} from '@angular/core';
import {
  ExtendedPerformanceAspect,
  PerformanceAspect,
  PerformanceAspectType
} from "../../../../models/perfomance-aspect.model";
import {Observable} from "rxjs";
import {Application} from "../../../../models/application.model";
import {Page} from "../../../../models/page.model";
import {AspectConfigurationService} from "../../services/aspect-configuration.service";
import {filter, map} from "rxjs/operators";

@Component({
  selector: 'osm-aspect-metrics',
  templateUrl: './aspect-metrics.component.html',
  styleUrls: ['./aspect-metrics.component.scss']
})
export class AspectMetricsComponent implements OnInit {

  @Input() aspectType: PerformanceAspectType;
  @Input() application: Application;
  @Input() page: Page;
  @Input() browserId: number;

  aspectsToShow$: Observable<ExtendedPerformanceAspect[]>;

  constructor(private aspectConfService: AspectConfigurationService) {
  }

  ngOnInit() {
    // combineLatest(this.aspectConfService.extendedAspects$, this.aspectConfService.uniqueAspectTypes$).pipe(
    //   filter(([aspects, types]: [ExtendedPerformanceAspect[], PerformanceAspectType[]]) => types.length > 0),
    //   map(([aspects, types]: [ExtendedPerformanceAspect[], PerformanceAspectType[]]) => aspects)
    // ).subscribe((aspects: ExtendedPerformanceAspect[]) => console.log(`############## aspects=${JSON.stringify(aspects)}\nthis.aspectType=${JSON.stringify(this.aspectType)}\nthis.application=${JSON.stringify(this.application)}\nthis.page=${JSON.stringify(this.page)}\nthis.browserId=${JSON.stringify(this.browserId)}`));

    this.aspectsToShow$ = this.aspectConfService.extendedAspects$.pipe(
      filter((aspects: ExtendedPerformanceAspect[]) => {
        console.log(`aspects=${JSON.stringify(aspects)}\nthis.aspectType=${JSON.stringify(this.aspectType)}\nthis.application=${JSON.stringify(this.application)}\nthis.page=${JSON.stringify(this.page)}\nthis.browserId=${JSON.stringify(this.browserId)}`);
        return aspects.length > 0 && typeof this.aspectType !== 'undefined' && this.aspectType != null
      }),
      map((aspects: ExtendedPerformanceAspect[]) => {
        return aspects.filter((aspect: ExtendedPerformanceAspect) => aspect.performanceAspectType.name == this.aspectType.name)
      })
    );
    this.aspectsToShow$.subscribe((aspects: ExtendedPerformanceAspect[]) => console.log(`aspectsToShow=${JSON.stringify(aspects)}`))
  }

  getSelectedAspect(): PerformanceAspect {
    const matchingAspect: PerformanceAspect = this.aspectConfService.extendedAspects$.getValue().find((aspect: PerformanceAspect) => {
      return aspect.applicationId == this.application.id && aspect.pageId == this.page.id &&
        aspect.browserId == this.browserId && aspect.performanceAspectType.name == this.aspectType.name
    });
    return matchingAspect;
  }

}
