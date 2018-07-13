import {Component, Input} from '@angular/core';
import {CsiService} from "../../services/csi.service";
import {JobGroupDTO} from "../../../shared/models/job-group.model";
import {CsiDTO} from "../../models/csi.model";
import {Observable} from "rxjs/index";
import {map} from "rxjs/internal/operators";
import {ApplicationCsiListDTO} from "../../models/csi-list.model";

@Component({
  selector: 'osm-application-csi',
  templateUrl: './application-csi.component.html',
  styleUrls: ['./application-csi.component.scss']
})
export class ApplicationCsiComponent {
  recentCsiValue$: Observable<CsiDTO>;
  hasConfiguration$: Observable<boolean>;

  @Input()
  set application(application: JobGroupDTO) {
    this.csiService.getCsiForApplication(application);
  }

  constructor(private csiService: CsiService) {
    this.recentCsiValue$ = this.csiService.csiValues$.pipe(
      map((res: ApplicationCsiListDTO) => res.csiDtoList.slice(-1)[0]));

    this.hasConfiguration$ = this.csiService.csiValues$.pipe(
      map((res: ApplicationCsiListDTO) => res.hasCsiConfiguration));
  }
}
