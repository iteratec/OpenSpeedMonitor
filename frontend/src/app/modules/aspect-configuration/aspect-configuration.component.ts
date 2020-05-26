import {Component, OnInit} from '@angular/core';
import {ApplicationService} from '../../services/application.service';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {Page} from '../../models/page.model';
import {Observable} from 'rxjs';
import {ExtendedPerformanceAspect, PerformanceAspectType} from '../../models/perfomance-aspect.model';
import {Application} from '../../models/application.model';
import {AspectConfigurationService} from './services/aspect-configuration.service';
import {PerformanceAspectService} from '../../services/performance-aspect.service';
import {ResponseWithLoadingState} from '../../models/response-with-loading-state.model';
import {TitleService} from '../../services/title.service';

@Component({
  selector: 'osm-aspect-configuration',
  templateUrl: './aspect-configuration.component.html',
  styleUrls: ['./aspect-configuration.component.scss']
})
export class AspectConfigurationComponent implements OnInit {

  application$: Observable<Application>;
  page$: Observable<Page>;

  performanceAspects$: Observable<ExtendedPerformanceAspect[]>;
  aspectTypes$: Observable<ResponseWithLoadingState<PerformanceAspectType[]>>;

  constructor(
    private route: ActivatedRoute,
    private applicationService: ApplicationService,
    private aspectConfService: AspectConfigurationService,
    private perfAspectService: PerformanceAspectService,
    private titleService: TitleService
  ) {
    this.application$ = applicationService.selectedApplication$;
    this.page$ = aspectConfService.selectedPage$;
    this.performanceAspects$ = aspectConfService.extendedAspects$;
    this.aspectTypes$ = perfAspectService.aspectTypes$;
  }

  ngOnInit() {
    this.titleService.setTitle('frontend.de.iteratec.osm.performance-aspect.configuration.overview.title');
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.aspectConfService.loadApplication(params.get('applicationId'));
      this.aspectConfService.loadPage(params.get('pageId'));
    });
  }

  deselectPage() {
    this.aspectConfService.selectedPage$.next({id: -1, name: ''});
  }
}
