import {Component, Input} from '@angular/core';
import {FailingJobStatistic} from "../../models/failing-job-statistic.model";
import {Application} from "../../../../models/application.model";

@Component({
  selector: 'osm-application-job-status',
  templateUrl: './application-job-status.component.html',
  styleUrls: ['./application-job-status.component.scss']
})
export class ApplicationJobStatusComponent {
  @Input() failingJobStatistic: FailingJobStatistic;
  @Input() selectedApplication: Application;
}
