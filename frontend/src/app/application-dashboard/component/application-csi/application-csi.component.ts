import {Component, Input} from '@angular/core';
import {CsiService} from "../../service/rest/csi.service";
import {JobGroupDTO} from "../../../shared/model/job-group.model";
import {CsiDTO} from "../../model/csi.model";
import {CsiListDTO} from "../../model/csi-list.model";
import {Observable} from "rxjs/index";
import {map} from "rxjs/internal/operators";

@Component({
  selector: 'osm-application-csi',
  templateUrl: './application-csi.component.html',
  styleUrls: ['./application-csi.component.css']
})
export class ApplicationCsiComponent {
  recentCsiValue$: Observable<CsiDTO>;
  hasConfiguration$: Observable<boolean>;

  @Input()
  set jobGroup(jobGroup: JobGroupDTO) {
    this.csiService.getCsiForJobGroup(jobGroup);
  }

  constructor(private csiService: CsiService) {
    this.recentCsiValue$ = this.csiService.csiValues$.pipe(
      map((res: CsiListDTO) => res.jobGroupCsiDtos.slice(-1)[0]));

    this.hasConfiguration$ = this.csiService.csiValues$.pipe(
      map((res: CsiListDTO) => res.hasCsiConfiguration));
  }
}
