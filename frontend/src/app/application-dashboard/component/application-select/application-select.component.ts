import {Component, EventEmitter, Output} from '@angular/core';
import {JobGroupService} from "../../../setup-dashboard/service/rest/job-group.service";
import {Observable} from "rxjs/index";
import {JobGroupDTO} from "../../../common/model/job-group.model";

@Component({
  selector: 'osm-application-select',
  templateUrl: './application-select.component.html',
  styleUrls: ['./application-select.component.css']
})
export class ApplicationSelectComponent {
  @Output() onSelectedApplicationChanged: EventEmitter<JobGroupDTO> = new EventEmitter();
  jobGroups$: Observable<JobGroupDTO[]>;
  application: JobGroupDTO;

  constructor(private jobGroupService: JobGroupService) {
    this.jobGroups$ = jobGroupService.activeOrRecentlyMeasured$
  }

  onSelect() {
    console.log(this.application);
    this.onSelectedApplicationChanged.emit(this.application)
  }
}
