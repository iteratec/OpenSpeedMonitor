import {Component} from '@angular/core';
import {Observable} from "rxjs/index";
import {map} from "rxjs/internal/operators";
import {ApplicationCsiListDTO} from "../../models/csi-list.model";
import {ApplicationDashboardService} from "../../services/application-dashboard.service";
import {CsiDTO} from "../../models/csi.model";

@Component({
  selector: 'osm-application-csi',
  templateUrl: './application-csi.component.html',
  styleUrls: ['./application-csi.component.scss']
})
export class ApplicationCsiComponent {
  recentCsiValue$: Observable<number>;
  hasConfiguration$: Observable<boolean>;
  csiValues$: Observable<ApplicationCsiListDTO>;
  isLoading: boolean = true;

  constructor(private dashboardService: ApplicationDashboardService) {
    this.csiValues$ = this.dashboardService.csiValues$;

    this.recentCsiValue$ = this.dashboardService.csiValues$.pipe(
      map((res: ApplicationCsiListDTO) => {
        this.isLoading = res.isLoading;
        const csiDto: CsiDTO = res.csiDtoList.slice(-1)[0];
        return csiDto ? csiDto.csiDocComplete : null;
      }));

    this.hasConfiguration$ = this.dashboardService.csiValues$.pipe(
      map((res: ApplicationCsiListDTO) => res.hasCsiConfiguration));
  }
}
