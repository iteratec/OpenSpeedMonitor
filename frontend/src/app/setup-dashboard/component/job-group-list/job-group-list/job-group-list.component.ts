import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {JobGroupDTO} from "../../../../application-dashboard/model/job-group.model";
import {JobGroupService} from "../../../service/job-group.service";

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
