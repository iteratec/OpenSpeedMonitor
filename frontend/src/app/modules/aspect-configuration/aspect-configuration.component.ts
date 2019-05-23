import {Component, OnInit} from '@angular/core';
import {ApplicationService} from "../../services/application.service";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {Page} from "../../models/page.model";
import {combineLatest, Observable} from "rxjs";
import {
  ExtendedPerformanceAspect,
  PerformanceAspect,
  PerformanceAspectType
} from "../../models/perfomance-aspect.model";
import {Application} from "../../models/application.model";
import {AspectConfigurationService} from "./services/aspect-configuration.service";
import {BrowserInfoDto} from "../../models/browser.model";
import {map} from "rxjs/operators";

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
    this.prepareExtensionOfAspects();
  }

  ngOnInit() {
    this.getBrowserInfos();
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.getApplication(params.get('applicationId'));
      this.getPage(params.get('pageId'));
    });
    this.initAspectTypes();
    // this.activateDebugging();
  }

  initAspectTypes() {
    this.aspectTypes$ = this.performanceAspects$.pipe(
      map((extendedAspects: ExtendedPerformanceAspect[]) => {
        const uniqueAspectTypes = [];
        const lookupMap = new Map();
        for (const aspect of extendedAspects) {
          if (!lookupMap.has(aspect.performanceAspectType.name)) {
            lookupMap.set(aspect.performanceAspectType.name, true);
            uniqueAspectTypes.push(aspect.performanceAspectType)
          }
        }
        return uniqueAspectTypes
      })
    )
  }

  private activateDebugging() {
    // this.application$.subscribe(app => console.log(`app=${JSON.stringify(app)}`));
    // this.page$.subscribe(page => console.log(`page=${JSON.stringify(page)}`));
    // this.applicationService.performanceAspectsForPage$.subscribe(aspects => console.log(`aspect=${JSON.stringify(aspects)}`));
    // this.aspectConfService.browserInfos$.subscribe(browserInfos => console.log(`browserInfos=${browserInfos}`));
    this.performanceAspects$.subscribe(extendedAspects => console.log(`extendedAspects=${JSON.stringify(extendedAspects)}`))
    this.aspectTypes$.subscribe(aspectTypes => console.log(`aspectTypes=${JSON.stringify(aspectTypes)}`))
  }

  private prepareExtensionOfAspects() {
    this.performanceAspects$ = combineLatest(this.applicationService.performanceAspectsForPage$, this.aspectConfService.browserInfos$).pipe(
      map(([aspects, browserInfos]: [PerformanceAspect[], BrowserInfoDto[]]) => {
        const extendedAspects = this.extendAspects(aspects, browserInfos);
        return extendedAspects;
      })
    )
  }

  private extendAspects(aspects: PerformanceAspect[], browserInfos: BrowserInfoDto[]) {
    const extendedAspects: ExtendedPerformanceAspect[] = [];
    if (aspects.length > 0 && browserInfos.length > 0) {
      aspects.forEach((aspect: PerformanceAspect) => {
        const additionalInfos = browserInfos.filter((browserInfo: BrowserInfoDto) => browserInfo.browserId == aspect.browserId)
        let extension: BrowserInfoDto;
        if (additionalInfos.length == 1) {
          extension = additionalInfos[0];
        } else {
          extension = {
            browserId: aspect.browserId,
            browserName: 'Unknown',
            operatingSystem: 'Unknown',
            deviceType: {name: 'Unknown', icon: 'question'}
          }
        }
        extendedAspects.push({...aspect, ...extension})
      });
    }
    return extendedAspects;
  }

  getApplication(applicationId: string) {
    this.aspectConfService.loadApplication(applicationId).subscribe((app: Application) => {
      this.applicationService.selectedApplication$.next(app);
    })
  }

  getPage(pageId: string) {
    this.aspectConfService.loadPage(pageId).subscribe((page: Page) => {
      this.applicationService.selectedPage$.next(page);
    })
  }

  getBrowserInfos() {
    this.aspectConfService.loadBrowserInfos()
  }
}
