import {Component} from '@angular/core';
import {JobGroupDTO} from "../../../../common/model/job-group.model";
import {Observable} from 'rxjs';
import {JobGroupService} from "../../../service/rest/job-group.service";

@Component({
  selector: 'app-job-group-list',
  templateUrl: './job-group-list.component.html',
  styleUrls: ['./job-group-list.component.css']
})
export class JobGroupListComponent {
  jobGroupList$: Observable<JobGroupDTO[]>;

  constructor(private jobGroupService: JobGroupService) {
    this.jobGroupList$ = this.jobGroupService.jobGroups$
  }
}
