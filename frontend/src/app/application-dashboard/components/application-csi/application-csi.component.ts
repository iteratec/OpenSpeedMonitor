import {Component} from '@angular/core';
import {CsiDTO} from "../../models/csi.model";
import {Observable} from "rxjs/index";
import {map} from "rxjs/internal/operators";
import {ApplicationCsiListDTO} from "../../models/csi-list.model";
import {ApplicationDashboardService} from "../../services/application-dashboard.service";

@Component({
  selector: 'osm-application-csi',
  templateUrl: './application-csi.component.html',
  styleUrls: ['./application-csi.component.scss']
})
export class ApplicationCsiComponent {
  recentCsiValue$: Observable<CsiDTO>;
  hasConfiguration$: Observable<boolean>;

  constructor(private dashboardService: ApplicationDashboardService) {
    this.recentCsiValue$ = this.dashboardService.csiValues$.pipe(
      map((res: ApplicationCsiListDTO) => res.csiDtoList.slice(-1)[0]));

    this.hasConfiguration$ = this.dashboardService.csiValues$.pipe(
      map((res: ApplicationCsiListDTO) => res.hasCsiConfiguration));
  }
}
