import {Component, EventEmitter, Output} from '@angular/core';
import {JobGroupService} from "../../../setup-dashboard/service/rest/job-group.service";
import {Observable} from "rxjs/index";
import {map, take} from "rxjs/internal/operators";
import {JobGroupDTO} from "../../../common/model/job-group.model";

@Component({
  selector: 'osm-application-select',
  templateUrl: './application-select.component.html',
  styleUrls: ['./application-select.component.css']
})
export class ApplicationSelectComponent {
  @Output() selectApplication: EventEmitter<JobGroupDTO> = new EventEmitter();
  jobGroups$: Observable<JobGroupDTO[]>;
  selectedApplication: JobGroupDTO;

  constructor(private jobGroupService: JobGroupService) {
    this.jobGroups$ = jobGroupService.activeOrRecentlyMeasured$.pipe(
      map((jobGroups: JobGroupDTO[]) =>
        jobGroups.sort((a, b) => a.name.localeCompare(b.name, [], {sensitivity: "base"})
        ))
    );
    this.jobGroups$
      .pipe(take(1))
      .subscribe((jobGroups: JobGroupDTO[]) => {
        this.selectedApplication = jobGroups[0];
      });
  }

  setApplication(jobGroup: JobGroupDTO) {
    this.selectedApplication = jobGroup;
    this.selectApplication.emit(jobGroup)
  }
}
