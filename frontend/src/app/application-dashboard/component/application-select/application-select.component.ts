import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {JobGroupService} from "../../../shared/service/rest/job-group.service";
import {Observable} from "rxjs/index";
import {map, take} from "rxjs/internal/operators";
import {JobGroupDTO} from "../../../shared/model/job-group.model";

@Component({
  selector: 'osm-application-select',
  templateUrl: './application-select.component.html',
  styleUrls: ['./application-select.component.css']
})
export class ApplicationSelectComponent implements OnInit {
  @Output() selectApplication: EventEmitter<JobGroupDTO> = new EventEmitter();
  @Input() jobGroupId: number;
  jobGroups$: Observable<JobGroupDTO[]>;
  selectedApplication: JobGroupDTO;

  constructor(private jobGroupService: JobGroupService) {
    this.jobGroups$ = jobGroupService.activeOrRecentlyMeasured$.pipe(
      map((jobGroups: JobGroupDTO[]) =>
        jobGroups.sort((a, b) => a.name.localeCompare(b.name, [], {sensitivity: "base"})
        ))
    );
  }

  ngOnInit() {
    this.jobGroups$
      .pipe(take(1))
      .subscribe((jobGroups: JobGroupDTO[]) => {
        this.setApplication(this.lookForJobGroupWithId(jobGroups, this.jobGroupId));
      });
  }

  setApplication(jobGroup: JobGroupDTO) {
    this.selectedApplication = jobGroup;
    this.selectApplication.emit(jobGroup)
  }

  lookForJobGroupWithId(jobGroups: JobGroupDTO[], jobGroupId: number) {
    return (jobGroups.filter(jobGroup => jobGroup.id === jobGroupId).length === 1) ?
      jobGroups.filter(jobGroup => jobGroup.id === jobGroupId)[0] : jobGroups[0];
  }
}
