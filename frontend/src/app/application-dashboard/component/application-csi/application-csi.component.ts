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
  recentCsiValue: CsiDTO;
  @Input() jobGroup: JobGroupDTO;

  constructor(private csiService: CsiService) {
    console.log(this.jobGroup);

    this.csiService.getCsiForJobGroup(this.jobGroup);

    this.csiValues$ = this.csiService.csiValues$.pipe();

    this.csiValues$.pipe()
      .subscribe((csiValues: CsiDTO[]) => {
        console.log(csiValues);
        this.recentCsiValue = csiValues[csiValues.length - 1]
      })
  }
}
