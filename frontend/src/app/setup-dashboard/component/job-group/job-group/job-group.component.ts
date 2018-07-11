import {Component, Input} from '@angular/core';
import {ApplicationDTO} from "../../../../application-dashboard/model/application.model";


@Component({
  selector: 'osm-job-group',
  templateUrl: './job-group.component.html',
  styleUrls: ['./job-group.component.scss']
})
export class JobGroupComponent {
  @Input() jobGroup: ApplicationDTO;

  constructor() {
  }
}
