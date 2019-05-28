import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {catchError, map} from "rxjs/operators";
import {BehaviorSubject, combineLatest, EMPTY} from "rxjs";
import {Page} from "../../../models/page.model";
import {BrowserInfoDto} from "../../../models/browser.model";
import {ApplicationService} from "../../../services/application.service";
import {
  ExtendedPerformanceAspect,
  PerformanceAspect,
  PerformanceAspectType
} from "../../../models/perfomance-aspect.model";

@Injectable({
  providedIn: 'root'
})
export class AspectConfigurationService {

  browserInfos$ = new BehaviorSubject<BrowserInfoDto[]>([]);
  performanceAspects$ = new BehaviorSubject<ExtendedPerformanceAspect[]>([]);
  uniqueAspectTypes$ = new BehaviorSubject<PerformanceAspectType[]>([]);

  constructor(private http: HttpClient, private applicationService: ApplicationService) {
    this.loadBrowserInfos();
  }

  loadApplication(applicationId: string): void {
    this.applicationService.setSelectedApplication(applicationId)
  }

  loadPage(pageId: string): void {
    this.http.get<Page>(
      '/aspectConfiguration/rest/getPage',
      {params: {pageId: pageId}}).pipe(
      catchError((error) => {
        console.error(error);
        return EMPTY;
      })
    ).subscribe((page: Page) => {
      this.applicationService.selectedPage$.next(page);
    })
  }

  loadBrowserInfos() {
    this.http.get<BrowserInfoDto[]>(
      '/aspectConfiguration/rest/getBrowserInformations').pipe(
      catchError((error) => {
        console.error(error);
        return EMPTY;
      })
    ).subscribe(nextBrowserInfo => this.browserInfos$.next(nextBrowserInfo))
  }

  prepareExtensionOfAspects() {
    combineLatest(this.applicationService.performanceAspectsForPage$, this.browserInfos$).pipe(
      map(([aspects, browserInfos]: [PerformanceAspect[], BrowserInfoDto[]]) => {
        const extendedAspects: ExtendedPerformanceAspect[] = this.extendAspects(aspects, browserInfos);
        return extendedAspects;
      })
    ).subscribe((nextExtendedAspects: ExtendedPerformanceAspect[]) => this.performanceAspects$.next(nextExtendedAspects))
  }

  extendAspects(aspects: PerformanceAspect[], browserInfos: BrowserInfoDto[]): ExtendedPerformanceAspect[] {
    const extendedAspects: ExtendedPerformanceAspect[] = [];
    if (aspects.length > 0 && browserInfos.length > 0) {
      aspects.forEach((aspect: PerformanceAspect) => {
        const additionalInfos = browserInfos.filter((browserInfo: BrowserInfoDto) => browserInfo.browserId == aspect.browserId);
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

  initAspectTypes() {
    this.performanceAspects$.pipe(
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
    ).subscribe((uniqueAspectTypes: PerformanceAspectType[]) => this.uniqueAspectTypes$.next(uniqueAspectTypes))
  }
}
