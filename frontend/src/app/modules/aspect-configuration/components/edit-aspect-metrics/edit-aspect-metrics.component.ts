import {Component, OnInit} from '@angular/core';
import {Observable} from "rxjs";
import {Application} from "../../../../models/application.model";
import {Page} from "../../../../models/page.model";
import {ApplicationService} from "../../../../services/application.service";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {AspectConfigurationService} from "../../services/aspect-configuration.service";
import {ExtendedPerformanceAspect, PerformanceAspectType} from "../../../../models/perfomance-aspect.model";

@Component({
  selector: 'osm-edit-aspect-metrics',
  templateUrl: './edit-aspect-metrics.component.html',
  styleUrls: ['./edit-aspect-metrics.component.scss']
})
export class EditAspectMetricsComponent implements OnInit {

  application$: Observable<Application>;
  page$: Observable<Page>;
  performanceAspects$: Observable<ExtendedPerformanceAspect[]>;
  aspectType: PerformanceAspectType;

  constructor(private route: ActivatedRoute, private applicationService: ApplicationService, private aspectConfService: AspectConfigurationService) {
    this.application$ = applicationService.selectedApplication$;
    this.page$ = aspectConfService.selectedPage$;
    this.performanceAspects$ = aspectConfService.performanceAspects$
  }

  ngOnInit() {
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.aspectConfService.loadApplication(params.get('applicationId'));
      this.aspectConfService.loadPage(params.get('pageId'));

      console.log('aspectType=' + params.get('aspectType'));
      console.log('browserId=' + params.get('browserId'));
      this.aspectConfService.uniqueAspectTypes$.subscribe((aspectTypes: PerformanceAspectType[]) => {
        this.aspectType = aspectTypes.filter((type: PerformanceAspectType) => type.name == params.get('aspectType'))[0]
      })
    });

  }

}
