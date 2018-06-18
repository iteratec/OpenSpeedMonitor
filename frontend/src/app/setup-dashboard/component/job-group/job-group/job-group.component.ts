import {Component, Input} from '@angular/core';
import {JobGroupDTO} from "../../../../common/model/job-group.model";

@Component({
  selector: 'app-job-group',
  templateUrl: './job-group.component.html',
  styleUrls: ['./job-group.component.css']
})
export class JobGroupComponent {
  @Input() jobGroup: JobGroupDTO;

  constructor() {
  }
}
