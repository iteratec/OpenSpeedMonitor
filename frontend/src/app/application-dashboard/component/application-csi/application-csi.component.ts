import {Component, Input} from '@angular/core';
import {CsiService} from "../../service/rest/csi.service";
import {JobGroupDTO} from "../../../shared/model/job-group.model";
import {CsiDTO} from "../../model/csi.model";
import {CsiListDTO} from "../../model/csi-list.model";

@Component({
  selector: 'osm-application-csi',
  templateUrl: './application-csi.component.html',
  styleUrls: ['./application-csi.component.css']
})
export class ApplicationCsiComponent {
  csiValues: CsiListDTO;
  recentCsiValue: CsiDTO;

  @Input()
  set jobGroup(jobGroup: JobGroupDTO) {
    this.csiService.getCsiForJobGroup(jobGroup);

    this.csiService.csiValues$.subscribe((res: CsiListDTO) => {
      this.csiValues = res;
    });

    //TODO: Get recent csi value
    //this.recentCsiValue$ = this.csiValues$.pipe()
  }

  constructor(private csiService: CsiService) {
  }
}
