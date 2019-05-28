import {Component, OnInit} from '@angular/core';
import {ApplicationService} from "../../services/application.service";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {Page} from "../../models/page.model";
import {Observable} from "rxjs";
import {ExtendedPerformanceAspect, PerformanceAspectType} from "../../models/perfomance-aspect.model";
import {Application} from "../../models/application.model";
import {AspectConfigurationService} from "./services/aspect-configuration.service";

@Component({
  selector: 'osm-aspect-configuration',
  templateUrl: './aspect-configuration.component.html',
  styleUrls: ['./aspect-configuration.component.scss']
})
export class AspectConfigurationComponent implements OnInit {

  application$: Observable<Application>;
  page$: Observable<Page>;

  performanceAspects$: Observable<ExtendedPerformanceAspect[]>;
  aspectTypes$: Observable<PerformanceAspectType[]>;

  constructor(private route: ActivatedRoute, private applicationService: ApplicationService, private aspectConfService: AspectConfigurationService) {
    this.application$ = applicationService.selectedApplication$;
    this.page$ = applicationService.selectedPage$;
    this.performanceAspects$ = aspectConfService.performanceAspects$;
    this.aspectTypes$ = this.aspectConfService.uniqueAspectTypes$;
    this.aspectConfService.prepareExtensionOfAspects();
  }

  ngOnInit() {
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.aspectConfService.loadApplication(params.get('applicationId'));
      this.aspectConfService.loadPage(params.get('pageId'));
    });
    this.aspectConfService.initAspectTypes();
  }

}
