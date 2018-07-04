import {Component, Input} from '@angular/core';
import {CsiService} from "../../service/rest/csi.service";
import {JobGroupDTO} from "../../../shared/model/job-group.model";
import {CsiDTO} from "../../model/csi.model";
import {Observable} from "rxjs/index";
import {map} from "rxjs/internal/operators";
import {ApplicationCsiListDTO} from "../../model/csi-list.model";

@Component({
  selector: 'osm-application-csi',
  templateUrl: './application-csi.component.html',
  styleUrls: ['./application-csi.component.css']
})
export class ApplicationCsiComponent {
  recentCsiValue$: Observable<CsiDTO>;
  hasConfiguration$: Observable<boolean>;

  @Input()
  set application(application: JobGroupDTO) {
    this.csiService.getCsiForJobGroup(application);
  }

  constructor(private csiService: CsiService) {
    this.recentCsiValue$ = this.csiService.csiValues$.pipe(
      map((res: ApplicationCsiListDTO) => {
        return res.csiDtoList.slice(-1)[0]
      }));

    this.hasConfiguration$ = this.csiService.csiValues$.pipe(
      map((res: ApplicationCsiListDTO) => res.hasCsiConfiguration));
  }
}
