import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {FailingJobStatistic} from "../../models/failing-job-statistic.model";
import {Application} from "../../../../models/application.model";
import {ApplicationService} from "../../../../services/application.service";
import {Observable} from "rxjs";
import {JobHealthGraphiteServers} from "../../models/job-health-graphite-servers.model";

@Component({
  selector: 'osm-application-job-status',
  templateUrl: './application-job-status.component.html',
  styleUrls: ['./application-job-status.component.scss']
})
export class ApplicationJobStatusComponent implements OnChanges {
  @Input() failingJobStatistic: FailingJobStatistic;
  @Input() selectedApplication: Application;

  jobHealthGraphiteServers$: Observable<JobHealthGraphiteServers>;

  iconClass: string;
  infoText: string;
  jobStatus: string;

  hasFailingJobs: boolean;

  constructor(private applicationService: ApplicationService) {
    this.jobHealthGraphiteServers$ = this.applicationService.jobHealthGraphiteServers$;
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.setInformation();
  }

  private setInformation(): void {
    if (!this.failingJobStatistic) {
      this.infoText = 'frontend.de.iteratec.osm.applicationDashboard.jobStatus.noInformationAvailable';
      this.jobStatus = '';
      this.iconClass = 'far fa-question-circle';
      return
    }

    if (this.failingJobStatistic.numberOfFailingJobs === 0) {
      this.hasFailingJobs = false;
      this.infoText = 'frontend.de.iteratec.osm.applicationDashboard.jobStatus.allJobsRunning';
      this.jobStatus = 'job-status-good';
      this.iconClass = 'fas fa-check-circle';
      return
    }

    if (this.failingJobStatistic.minimumFailedJobSuccessRate >= 80 && this.failingJobStatistic.minimumFailedJobSuccessRate < 90) {
      this.jobStatus = 'job-status-warning';
      this.iconClass = 'fas fa-exclamation-circle';
      this.infoText = this.determineTextForNumberOfFailingJobs();
      return
    } else {
      this.jobStatus = 'job-status-error';
      this.iconClass = 'fas fa-exclamation-circle';
      this.infoText = this.determineTextForNumberOfFailingJobs();
      return
    }
  }

  private determineTextForNumberOfFailingJobs(): string {
    if (this.failingJobStatistic.numberOfFailingJobs === 1) {
      this.hasFailingJobs = true;
      return 'frontend.de.iteratec.osm.applicationDashboard.jobStatus.oneFailingJob';
    } else {
      this.hasFailingJobs = true;
      return 'frontend.de.iteratec.osm.applicationDashboard.jobStatus.multipleFailingJobs';
    }
  }
}
