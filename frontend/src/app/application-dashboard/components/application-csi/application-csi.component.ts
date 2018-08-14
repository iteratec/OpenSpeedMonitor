import {Component, Input} from '@angular/core';
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
  @Input() lastDateOfResults: string;

  recentCsiValue$: Observable<CsiDTO>;
  hasConfiguration$: Observable<boolean>;
  csiValues$: Observable<ApplicationCsiListDTO>;
  recentCsiDate$: Observable<string>;

  constructor(private dashboardService: ApplicationDashboardService) {
    this.csiValues$ = this.dashboardService.csiValues$;

    this.recentCsiValue$ = this.dashboardService.csiValues$.pipe(
      map((res: ApplicationCsiListDTO) => res.csiDtoList.slice(-1)[0]));

    this.recentCsiDate$ = this.dashboardService.csiValues$.pipe(
      map((res: ApplicationCsiListDTO) => res.csiDtoList.slice(-1)[0].date));

    this.hasConfiguration$ = this.dashboardService.csiValues$.pipe(
      map((res: ApplicationCsiListDTO) => res.hasCsiConfiguration));
  }

  generateToolTip(lastDate: string): string {
    console.log(this.lastDateOfResults);
    if (lastDate < new Date(this.lastDateOfResults).toISOString().substring(0, 10)) {
      return "This value is from " + new Date(lastDate).toLocaleDateString("de-DE") + ",\nas there are no measurements today.";
    }
    return "";
  }
}
