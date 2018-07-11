import {Component, Input} from '@angular/core';
import {CsiService} from "../../service/csi.service";
import {CsiDTO} from "../../model/csi.model";
import {Observable} from "rxjs/index";
import {map} from "rxjs/internal/operators";
import {ApplicationCsiListDTO} from "../../model/csi-list.model";
import {JobGroupDTO} from "../../model/job-group.model";

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
