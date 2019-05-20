import {Component, OnInit} from '@angular/core';
import {ApplicationService} from "../../services/application.service";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {Page} from "../../models/page.model";
import {Observable, Subject} from "rxjs";
import {ResponseWithLoadingState} from "../../models/response-with-loading-state.model";
import {PerformanceAspect} from "../../models/perfomance-aspect.model";
import {Application} from "../../models/application.model";
import {AspectConfigurationService} from "./services/aspect-configuration.service";
import {Loading} from "../../models/Loading";

@Component({
  selector: 'osm-aspect-configuration',
  templateUrl: './aspect-configuration.component.html',
  styleUrls: ['./aspect-configuration.component.scss']
})
export class AspectConfigurationComponent implements OnInit {

  application$: Observable<Application>;
  page$: Observable<Page>;

  performanceAspects$: Subject<ResponseWithLoadingState<PerformanceAspect>[]>;

  constructor(private route: ActivatedRoute, private applicationService: ApplicationService, private service: AspectConfigurationService) {
    this.application$ = applicationService.selectedApplication$;
    this.page$ = applicationService.selectedPage$;
    this.performanceAspects$ = this.applicationService.performanceAspectForPage$;
  }

  ngOnInit() {
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.getApplication(params.get('applicationId'));
      this.getPage(params.get('pageId'));
    });
  }

  getApplication(applicationId: string) {
    this.service.loadApplication(applicationId).subscribe((app: Application & Loading) => {
      this.applicationService.selectedApplication$.next(app);
    })
  }

  getPage(pageId: string) {
    this.service.loadPage(pageId).subscribe((page: Page & Loading) => {
      this.applicationService.selectedPage$.next(page);
    })
  }
}
