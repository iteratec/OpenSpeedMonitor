import {Component, OnDestroy} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable, Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {ApplicationDashboardService} from './services/application-dashboard.service';
import {ApplicationDTO} from './models/application.model';
import {PageMetricsDto} from "./models/page-metrics.model";
import {ResponseWithLoadingState} from "./models/response-with-loading-state.model";
import {ApplicationCsiListDTO} from "./models/csi-list.model";
import {CsiDTO} from "./models/csi.model";

@Component({
  selector: 'osm-application-dashboard',
  templateUrl: './application-dashboard.component.html',
  styleUrls: ['./application-dashboard.component.scss']
})
export class ApplicationDashboardComponent implements OnDestroy {
  applications$: Observable<ApplicationDTO[]>;
  selectedApplication: ApplicationDTO;
  destroyed$ = new Subject<void>();
  pages$: Observable<PageMetricsDto[]>;
  csiValues$: Observable<ApplicationCsiListDTO>;
  recentCsiDate$: Observable<string>;
  recentCsiValue$: Observable<CsiDTO>;
  hasConfiguration$: Observable<boolean>;
  isLoading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private dashboardService: ApplicationDashboardService
  ) {
    this.pages$ = this.dashboardService.metrics$;
    this.applications$ = dashboardService.activeOrRecentlyMeasured$.pipe(
      map((applications: ApplicationDTO[]) => this.sortApplicationsByName(applications))
    );

    this.csiValues$ = this.dashboardService.csiValues$.pipe(
      map((res: ResponseWithLoadingState<ApplicationCsiListDTO>) => {
        return res.data;
      }));

    this.recentCsiValue$ = this.dashboardService.csiValues$.pipe(
      map((res: ResponseWithLoadingState<ApplicationCsiListDTO>) => {
        this.isLoading = res.isLoading;
        const csiDto: CsiDTO = res.data.csiDtoList.slice(-1)[0];
        return csiDto ? csiDto : null;
      }));

    this.recentCsiDate$ = this.dashboardService.csiValues$.pipe(
      map((res: ResponseWithLoadingState<ApplicationCsiListDTO>) => {
        const csiDto: CsiDTO = res.data.csiDtoList.slice(-1)[0];
        return csiDto ? csiDto.date : null;
      }));

    this.hasConfiguration$ = this.dashboardService.csiValues$.pipe(
      map((res: ResponseWithLoadingState<ApplicationCsiListDTO>) => res.data.hasCsiConfiguration));

    combineLatest(this.route.paramMap, this.applications$)
      .pipe(takeUntil(this.destroyed$))
      .subscribe(([navParams, applications]) => this.handleNavigation(navParams.get('applicationId'), applications));
  }

  private handleNavigation(applicationId: string, applications: ApplicationDTO[]) {
    if (!applicationId) {
      this.updateApplication(applications[0]);
      return;
    }
    this.selectedApplication = this.findApplicationById(applications, applicationId);
    if (this.selectedApplication) {
      this.dashboardService.updateApplicationData(this.selectedApplication);
    }
  }

  ngOnDestroy() {
    this.destroyed$.next(null);
    this.destroyed$.complete();
  }

  updateApplication(application: ApplicationDTO) {
    this.router.navigate(['/applicationDashboard', application.id]);
  }

  private findApplicationById(applications: ApplicationDTO[], applicationId: string) {
    return applications.find(application => application.id == Number(applicationId));
  }

  private sortApplicationsByName(applications: ApplicationDTO[]) {
    return applications.sort((a, b) => a.name.localeCompare(b.name, [], {sensitivity: 'base'}));
  }
}
