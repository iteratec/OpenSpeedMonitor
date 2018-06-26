import {Component, Input} from '@angular/core';
import {CsiService} from "../../service/rest/csi.service";
import {JobGroupDTO} from "../../../shared/model/job-group.model";
import {Observable} from "rxjs/internal/Observable";
import {CsiDTO} from "../../model/csi.model";

@Component({
  selector: 'osm-application-csi',
  templateUrl: './application-csi.component.html',
  styleUrls: ['./application-csi.component.css']
})
export class ApplicationCsiComponent {
  csiValues$: Observable<CsiDTO[]>;
  recentCsiValue$: Observable<CsiDTO>;

  @Input()
  set jobGroup(jobGroup: JobGroupDTO) {
    this.csiService.getCsiForJobGroup(jobGroup);

    this.csiValues$ = this.csiService.csiValues$.pipe();
    
    //TODO: Get recent csi value
    //this.recentCsiValue$ = this.csiValues$.pipe()
  }

  constructor(private csiService: CsiService) {
  }
}
