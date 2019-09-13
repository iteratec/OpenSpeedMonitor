import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {
  catchError,
  filter,
  map,
  mergeMap,
  switchMap,
  withLatestFrom
} from "rxjs/operators";
import {BehaviorSubject, combineLatest, EMPTY, Observable, ReplaySubject} from "rxjs";
import {Page} from "../../../models/page.model";
import {BrowserInfoDto} from "../../../models/browser.model";
import {ApplicationService} from "../../../services/application.service";
import {
  ExtendedPerformanceAspect,
  PerformanceAspect,
  PerformanceAspectType
} from "../../../models/perfomance-aspect.model";
import {Application} from "../../../models/application.model";
import {LocationDto} from "../../application-dashboard/models/location.model";
import {PerformanceAspectService} from "../../../services/performance-aspect.service";
import {ResponseWithLoadingState} from "../../../models/response-with-loading-state.model";

@Injectable({
  providedIn: 'root'
})
export class AspectConfigurationService {

  performanceAspectsForPage$ = new BehaviorSubject<PerformanceAspect[]>([]);
  browserInfos$ = new BehaviorSubject<BrowserInfoDto[]>([]);
  extendedAspects$ = new BehaviorSubject<ExtendedPerformanceAspect[]>([]);

  selectedPage$ = new ReplaySubject<Page>(1);
  selectedAspectType$ = new ReplaySubject<PerformanceAspectType>(1);

  constructor(private http: HttpClient, private applicationService: ApplicationService, private perfAspectService: PerformanceAspectService) {
    this.prepareExtensionOfAspects();
    this.loadBrowserInfos();
    this.getPerfAspectParams()
      .pipe(
        switchMap(perfAspectParams => this.getPerformanceAspects(perfAspectParams))
      ).subscribe(nextAspects => {
      this.performanceAspectsForPage$.next(nextAspects)
    });
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
      this.selectedPage$.next(page);
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
    this.performanceAspectsForPage$.pipe(
      withLatestFrom(this.browserInfos$),
      map(([aspects, browserInfos]: [PerformanceAspect[], BrowserInfoDto[]]) => {
        const extendedAspects: ExtendedPerformanceAspect[] = this.extendAspects(aspects, browserInfos);
        return extendedAspects;
      })
    ).subscribe((nextExtendedAspects: ExtendedPerformanceAspect[]) => {
      this.extendedAspects$.next(nextExtendedAspects)
    });
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

  public initSelectedAspectType(typeName: string) {
    this.perfAspectService.aspectTypes$.pipe(
      map((types: ResponseWithLoadingState<PerformanceAspectType[]>) => {
        return types.data.find((type: PerformanceAspectType) => type.name == typeName)
      })
    ).subscribe((type: PerformanceAspectType) => this.selectedAspectType$.next(type));
  }

  private getPerfAspectParams(): Observable<any> {
    return combineLatest(this.applicationService.selectedApplication$, this.selectedPage$)
      .pipe(
        filter(([application, page]: [Application, Page]) => page.id !== -1 && page.name !== ""),
        mergeMap(([application, page]: [Application, Page]) => {
          const params = this.createLocationParams(application, page);
          return this.http.get<LocationDto[]>('/resultSelection/getLocations', {params}).pipe(
            map((locations: LocationDto[]) => this.generateParams(application, page, locations)))
        })
      );
  }

  createLocationParams(application: Application, page: Page) {
    let now: Date = new Date();
    let fourWeeksAgo: Date = new Date();
    fourWeeksAgo.setDate(fourWeeksAgo.getDate() - 28);
    return {
      jobGroupIds: application.id.toString(),
      pageIds: page.id.toString(),
      from: fourWeeksAgo.toISOString(),
      to: now.toISOString()
    };
  }

  private generateParams(application: Application, page: Page, locations: LocationDto[]) {
    return {
      applicationId: application.id,
      pageId: page.id,
      browserIds: locations.map(loc => loc.parent.id)
    }
  }

  private getPerformanceAspects(params): Observable<PerformanceAspect[]> {
    return this.http.get<PerformanceAspect[]>('/applicationDashboard/rest/getPerformanceAspectsForApplication', {params})
      .pipe(
        catchError((error) => {
            console.error(error);
            return EMPTY;
          }
        ))
  }

  createOrUpdatePerformanceAspect(perfAspectToCreateOrUpdate: PerformanceAspect) {
    this.replacePerformanceAspect(perfAspectToCreateOrUpdate, true);
    const params = {
      performanceAspectId: perfAspectToCreateOrUpdate.id,
      pageId: perfAspectToCreateOrUpdate.pageId,
      applicationId: perfAspectToCreateOrUpdate.applicationId,
      browserId: perfAspectToCreateOrUpdate.browserId,
      performanceAspectType: perfAspectToCreateOrUpdate.performanceAspectType.name,
      metricIdentifier: perfAspectToCreateOrUpdate.measurand.id
    };
    this.http.post<PerformanceAspect>('/applicationDashboard/rest/createOrUpdatePerformanceAspect', params)
      .pipe(
        catchError((error) => {
          console.error(error);
          return EMPTY;
        }))
      .subscribe((createdAspect: PerformanceAspect) => this.replacePerformanceAspect(createdAspect, false))
  }


  private replacePerformanceAspect(perfAspectToReplace: PerformanceAspect, isLoading: boolean) {
    let prevValue: PerformanceAspect[] = this.performanceAspectsForPage$.getValue();
    let existingAspect: PerformanceAspect = prevValue.find((exAspect: PerformanceAspect) => {
      return exAspect.id == perfAspectToReplace.id &&
        exAspect.performanceAspectType == perfAspectToReplace.performanceAspectType &&
        exAspect.pageId == perfAspectToReplace.pageId &&
        exAspect.applicationId == perfAspectToReplace.applicationId
    });
    if (existingAspect) {
      prevValue = this.performanceAspectsForPage$.getValue();
      prevValue[prevValue.indexOf(existingAspect)] = perfAspectToReplace;
      this.performanceAspectsForPage$.next(prevValue);
    }
  }

}
