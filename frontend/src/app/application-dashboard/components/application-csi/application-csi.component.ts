import {Component, Input} from '@angular/core';
import {Observable} from "rxjs/index";
import {map} from "rxjs/internal/operators";
import {ApplicationCsiListDTO} from "../../models/csi-list.model";
import {ApplicationDashboardService} from "../../services/application-dashboard.service";
import {CsiDTO} from "../../models/csi.model";
import {ResponseWithLoadingState} from "../../models/response-with-loading-state.model";
import {ApplicationDTO} from "../../models/application.model";

@Component({
  selector: 'osm-application-csi',
  templateUrl: './application-csi.component.html',
  styleUrls: ['./application-csi.component.scss']
})
export class ApplicationCsiComponent {
  @Input() lastDateOfResults: string;
  @Input() selectedApplication: ApplicationDTO;
  csiValues$: Observable<ApplicationCsiListDTO>;
  recentCsiDate$: Observable<string>;
  recentCsiValue$: Observable<CsiDTO>;
  hasConfiguration$: Observable<boolean>;
  isLoading: boolean = true;

  constructor(private dashboardService: ApplicationDashboardService) {
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
  }
}
