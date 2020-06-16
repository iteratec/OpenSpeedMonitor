import {Component, OnInit} from '@angular/core';
import {JobResultDataService} from './services/job-result-data.service';
import {JobResult} from './models/job-result.model';
import {Job} from './models/job.model';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {StatusGroup} from './models/status-group.enum';
import {JobResultStatus} from './models/job-result-status.enum';
import {WptStatus} from './models/wpt-status.enum';

@Component({
  selector: 'osm-job-result',
  templateUrl: './job-result.component.html',
  styleUrls: ['./job-result.component.scss']
})
export class JobResultComponent implements OnInit {

  jobs: Job[] = [];
  jobResults: JobResult[] = [];
  allJobResultStatus: string[] = Object.values(JobResultStatus);
  allWptStatus: string[] = Object.values(WptStatus);

  filteredJobResults: JobResult[] = [];
  selectedJob: Job = null;
  currentSortingRule: { [key: string]: string } = {column: 'date', direction: 'desc'};
  filter: { [key: string]: any } = {testAgent: '', jobResultStatus: [], wptStatus: [], description: ''};

  constructor(private dataService: JobResultDataService,
              private route: ActivatedRoute,
              private router: Router,
              private translateService: TranslateService) {
  }

  jobResultStatusGroupByFn = (test: string): string => this.getJobResultStatusGroupName(test);

  wptStatusGroupByFn = (test: string): string => this.getWptStatusGroupName(test);

  groupValueFn = (groupName: string, children: any[]): any[] => children;

  ngOnInit() {
    this.getAllJobs();
  }

  getAllJobs(): void {
    this.dataService.getAllJobs()
      .subscribe((jobs: Job[]) => {
        this.jobs = jobs;
        this.readQueryParams();
      });
  }

  setJob(job: Job): void {
    if (job) {
      this.writeQueryParams(job.id);
      this.getJobResults(job.id);
    } else {
      this.writeQueryParams(null);
      this.jobResults = [];
      this.filteredJobResults = [];
    }
  }

  sort(column: string) {
    if (column === this.currentSortingRule.column) {
      if (this.currentSortingRule.direction === 'desc') {
        this.currentSortingRule.direction = 'asc';
      } else if (this.currentSortingRule.direction === 'asc') {
        this.currentSortingRule.direction = 'desc';
      }
    } else {
      this.currentSortingRule.column = column;
      this.currentSortingRule.direction = 'asc';
    }

    this.filteredJobResults.sort((a: JobResult, b: JobResult) =>
      this.compareJobResults(a, b, this.currentSortingRule.column, this.currentSortingRule.direction)
    );
  }

  setFilter(): void {
    this.filteredJobResults = this.jobResults.filter((jobResult: JobResult) => {
      if (this.arePropertiesThatAreFilteredEmpty(jobResult)) {
        return false;
      }
      return this.isJobResultIncludingTerms(jobResult);
    });
  }

  isTestNotTerminated(jobResultStatus: string): boolean {
    const notTerminatedStatus: string[] = [
      JobResultStatus.WAITING,
      JobResultStatus.RUNNING
    ];
    return notTerminatedStatus.includes(jobResultStatus);
  }

  isTestSuccessful(jobResultStatus: string): boolean {
    const successfulStatus: string[] = [
      JobResultStatus.SUCCESS
    ];
    return successfulStatus.includes(jobResultStatus);
  }

  hasTestFailed(jobResultStatus: string): boolean {
    const failedStatus: string[] = [
      JobResultStatus.INCOMPLETE,
      JobResultStatus.LAUNCH_ERROR,
      JobResultStatus.FETCH_ERROR,
      JobResultStatus.PERSISTENCE_ERROR,
      JobResultStatus.TIMEOUT,
      JobResultStatus.FAILED,
      JobResultStatus.CANCELED,
      JobResultStatus.ORPHANED,
      JobResultStatus.DID_NOT_START
    ];
    return failedStatus.includes(jobResultStatus);
  }

  private getJobResultStatusGroupName(test: string): string {
    if (this.isTestNotTerminated(test)) {
      return this.translateService.instant(StatusGroup.NOT_TERMINATED);
    }
    if (this.isTestSuccessful(test)) {
      return this.translateService.instant(StatusGroup.SUCCESS);
    }
    if (this.hasTestFailed(test)) {
      return this.translateService.instant(StatusGroup.FAILED);
    }
  }

  private getWptStatusGroupName(test: string): string {
    if (this.isWptNotTerminated(test)) {
      return this.translateService.instant(StatusGroup.NOT_TERMINATED);
    }
    if (this.isWptSuccessful(test)) {
      return this.translateService.instant(StatusGroup.SUCCESS);
    }
    if (this.hasWptFailed(test)) {
      return this.translateService.instant(StatusGroup.FAILED);
    }
  }

  private readQueryParams(): void {
    this.route.queryParams.subscribe((params: Params) => {
      if (this.checkQuery(params)) {
        const jobId = parseInt(params.job, 10);
        this.selectedJob = this.jobs.find((job: Job) => job.id === jobId);
        this.getJobResults(jobId);
      }
    });
  }

  private writeQueryParams(jobId: number): void {
    this.router.navigate([], {
      queryParams: {
        job: jobId
      }
    });
  }

  private getJobResults(jobId: number): void {
    this.dataService.getJobResults(jobId)
      .subscribe((jobResults: JobResult[]) => {
        this.jobResults = jobResults;
        this.filteredJobResults = jobResults;
      });
  }

  private compareJobResults(a: JobResult, b: JobResult, column: string, direction: string): number {
    let directionFactor = 1;
    if (direction === 'desc') {
      directionFactor = -1;
    }

    if (a[column] && b[column]) {
      if (a[column] < b[column]) {
        return -directionFactor;
      }
      if (a[column] > b[column]) {
        return directionFactor;
      }
    } else {
      if (a[column]) {
        return directionFactor;
      }
      if (b[column]) {
        return -directionFactor;
      }
    }
    return 0;
  }

  private checkQuery(params: Params): boolean {
    if (params) {
      return !!params.job;
    }
    return false;
  }

  private arePropertiesThatAreFilteredEmpty(jobResult: JobResult): boolean {
    if (!jobResult.testAgent && this.filter.testAgent) {
      return true;
    }
    if (!jobResult.jobResultStatus && this.filter.jobResultStatus.length > 0) {
      return true;
    }
    if (!jobResult.wptStatus && this.filter.wptStatus.length > 0) {
      return true;
    }
    if (!jobResult.description && this.filter.description) {
      return true;
    }
    return false;
  }

  private isJobResultIncludingTerms(jobResult: JobResult): boolean {
    if (jobResult.testAgent && !jobResult.testAgent.toLowerCase().includes(this.filter.testAgent.toLowerCase())) {
      return false;
    }
    if (jobResult.jobResultStatus && this.filter.jobResultStatus.length > 0) {
      if (!this.isStatusIncludingTerms(jobResult.jobResultStatus, this.filter.jobResultStatus)) {
        return false;
      }
    }
    if (jobResult.wptStatus && this.filter.wptStatus.length > 0) {
      if (!this.isStatusIncludingTerms(jobResult.wptStatus, this.filter.wptStatus)) {
        return false;
      }
    }
    if (jobResult.description && !jobResult.description.toLowerCase().includes(this.filter.description.toLowerCase())) {
      return false;
    }
    return true;
  }

  private isStatusIncludingTerms(status: string, terms: any): boolean {
    return terms.find((termOrTermList: (string | string[])) => {
      if (Array.isArray(termOrTermList)) {
        return termOrTermList.find((term: string) => status.toLowerCase().includes(term.toLowerCase()));
      }
      if (typeof termOrTermList === 'string') {
        return status.toLowerCase().includes(termOrTermList.toLowerCase());
      }
      return false;
    });
  }

  private isWptNotTerminated(wptStatus: string): boolean {
    const notTerminatedStatus: string[] = [
      WptStatus.UNKNOWN,
      WptStatus.PENDING,
      WptStatus.IN_PROGRESS
    ];
    return notTerminatedStatus.includes(wptStatus);
  }

  private isWptSuccessful(wptStatus: string): boolean {
    const successfulStatus: string[] = [
      WptStatus.SUCCESSFUL,
      WptStatus.COMPLETED,
      WptStatus.TEST_HAS_A_UNDEFINED_PROBLEM,
      WptStatus.TEST_COMPLETED_BUT_INDIVIDUAL_REQUEST_FAILED
    ];
    return successfulStatus.includes(wptStatus);
  }

  private hasWptFailed(wptStatus: string): boolean {
    const failedStatus: string[] = [
      WptStatus.TESTED_APPLICATION_CLIENT_ERROR,
      WptStatus.TESTED_APPLICATION_INTERNAL_SERVER_ERROR,
      WptStatus.TEST_DID_NOT_START,
      WptStatus.TEST_FAILED_WAITING_FOR_DOM_ELEMENT,
      WptStatus.TEST_TIMED_OUT,
      WptStatus.TEST_TIMED_OUT_CONTENT_ERRORS
    ];
    return failedStatus.includes(wptStatus);
  }
}
