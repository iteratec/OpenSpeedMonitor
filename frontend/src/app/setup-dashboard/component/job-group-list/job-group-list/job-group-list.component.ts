import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {ApplicationDTO} from "../../../../application-dashboard/model/application.model";
import {JobGroupService} from "../../../service/job-group.service";

@Component({
  selector: 'osm-job-group-list',
  templateUrl: './job-group-list.component.html',
  styleUrls: ['./job-group-list.component.scss']
})
export class JobGroupListComponent {
  jobGroupList$: Observable<ApplicationDTO[]>;

  constructor(private jobGroupService: JobGroupService) {
    this.jobGroupList$ = this.jobGroupService.jobGroups$
  }
}
