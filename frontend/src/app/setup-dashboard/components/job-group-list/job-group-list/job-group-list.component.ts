import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {JobGroupService} from "../../../services/job-group.service";
import {JobGroupDTO} from "../../../models/job-group.model";

@Component({
  selector: 'osm-job-group-list',
  templateUrl: './job-group-list.component.html',
  styleUrls: ['./job-group-list.component.scss']
})
export class JobGroupListComponent {
  jobGroupList$: Observable<JobGroupDTO[]>;

  constructor(private jobGroupService: JobGroupService) {
    this.jobGroupList$ = this.jobGroupService.jobGroups$
  }
}
