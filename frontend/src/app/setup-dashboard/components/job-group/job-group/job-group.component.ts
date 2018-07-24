import {Component, Input} from '@angular/core';
import {JobGroupDTO} from "../../../models/job-group.model";


@Component({
  selector: 'osm-job-group',
  templateUrl: './job-group.component.html',
  styleUrls: ['./job-group.component.scss']
})
export class JobGroupComponent {
  @Input() jobGroup: JobGroupDTO;

  constructor() {
  }
}
