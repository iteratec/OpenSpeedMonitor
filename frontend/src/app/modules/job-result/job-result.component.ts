import {Component, OnInit} from '@angular/core';
import {JobResultDataService} from './services/job-result-data.service';
import {JobResult} from './models/job-result.model';
import {Job} from './models/job.model';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {StatusGroup} from './models/status-group.enum';
import {JobResultStatus} from './models/job-result-status.enum';
import {WptStatus} from './models/wpt-status.enum';
import {JobResultFilter} from './models/job-result-filter.model';

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
  filter: JobResultFilter = {from: null, to: null, testAgent: '', jobResultStatus: [], wptStatus: [], description: ''};

  JOB_RESULT_STATUS_GROUPS: {[key: string]: string []} = {
    GROUP_NOT_TERMINATED: [
      JobResultStatus.WAITING,
      JobResultStatus.RUNNING
    ],
    GROUP_SUCCESS: [
      JobResultStatus.SUCCESS
    ],
    GROUP_FAILED: [
      JobResultStatus.INCOMPLETE,
      JobResultStatus.LAUNCH_ERROR,
      JobResultStatus.FETCH_ERROR,
      JobResultStatus.PERSISTENCE_ERROR,
      JobResultStatus.TIMEOUT,
      JobResultStatus.FAILED,
      JobResultStatus.CANCELED,
      JobResultStatus.ORPHANED,
      JobResultStatus.DID_NOT_START
    ]
  };

  WPT_STATUS_GROUPS: {[key: string]: string []} = {
    GROUP_NOT_TERMINATED: [
      WptStatus.UNKNOWN,
      WptStatus.PENDING,
      WptStatus.IN_PROGRESS
    ],
    GROUP_SUCCESS: [
      WptStatus.SUCCESSFUL,
      WptStatus.COMPLETED,
      WptStatus.TEST_HAS_A_UNDEFINED_PROBLEM,
      WptStatus.TEST_COMPLETED_BUT_INDIVIDUAL_REQUEST_FAILED
    ],
    GROUP_FAILED: [
      WptStatus.TESTED_APPLICATION_CLIENT_ERROR,
      WptStatus.TESTED_APPLICATION_INTERNAL_SERVER_ERROR,
      WptStatus.TEST_DID_NOT_START,
      WptStatus.TEST_FAILED_WAITING_FOR_DOM_ELEMENT,
      WptStatus.TEST_TIMED_OUT,
      WptStatus.TEST_TIMED_OUT_CONTENT_ERRORS
    ]
  };

  constructor(private dataService: JobResultDataService,
              private route: ActivatedRoute,
              private router: Router,
              private translateService: TranslateService) {
  }

  jobResultStatusGroupByFn = (jobResultStatus: string): string => this.getJobResultStatusGroupName(jobResultStatus);

  wptStatusGroupByFn = (wptStatus: string): string => this.getWptStatusGroupName(wptStatus);

  groupValueFn = (groupName: string, children: any[]): any => ({label: groupName, children: children});

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
      this.writeQueryParams();
      this.getJobResults(job.id);
    } else {
      this.writeQueryParams();
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
    this.writeQueryParams();
    this.filteredJobResults = this.jobResults.filter((jobResult: JobResult) => {
      if (this.arePropertiesThatAreFilteredEmpty(jobResult)) {
        return false;
      }
      return this.isJobResultIncludingTerms(jobResult);
    });
  }

  isTestNotTerminated(jobResultStatus: string): boolean {
    return this.JOB_RESULT_STATUS_GROUPS.GROUP_NOT_TERMINATED.includes(jobResultStatus);
  }

  isTestSuccessful(jobResultStatus: string): boolean {
    return this.JOB_RESULT_STATUS_GROUPS.GROUP_SUCCESS.includes(jobResultStatus);
  }

  hasTestFailed(jobResultStatus: string): boolean {
    return this.JOB_RESULT_STATUS_GROUPS.GROUP_FAILED.includes(jobResultStatus);
  }

  private getJobResultStatusGroupName(jobResultStatus: string): string {
    if (this.isTestNotTerminated(jobResultStatus)) {
      return this.translateService.instant(StatusGroup.GROUP_NOT_TERMINATED);
    }
    if (this.isTestSuccessful(jobResultStatus)) {
      return this.translateService.instant(StatusGroup.GROUP_SUCCESS);
    }
    if (this.hasTestFailed(jobResultStatus)) {
      return this.translateService.instant(StatusGroup.GROUP_FAILED);
    }
  }

  private getWptStatusGroupName(wptStatus: string): string {
    if (this.isWptNotTerminated(wptStatus)) {
      return this.translateService.instant(StatusGroup.GROUP_NOT_TERMINATED);
    }
    if (this.isWptSuccessful(wptStatus)) {
      return this.translateService.instant(StatusGroup.GROUP_SUCCESS);
    }
    if (this.hasWptFailed(wptStatus)) {
      return this.translateService.instant(StatusGroup.GROUP_FAILED);
    }
  }

  private readQueryParams(): void {
    this.route.queryParams.subscribe((params: Params) => {
      if (this.checkQuery(params)) {
        const jobId = parseInt(params.job, 10);
        if (jobId) {
          this.selectedJob = this.jobs.find((job: Job) => job.id === jobId);
          // this.filter.from = decodeURIComponent(params.from);
          // this.filter.to = decodeURIComponent(params.to);
          this.filter.testAgent = params.testAgent ? decodeURIComponent(params.testAgent) : '';
          this.filter.jobResultStatus = this.readStatusByQueryParam(params.status, JobResultStatus, this.JOB_RESULT_STATUS_GROUPS);
          this.filter.wptStatus = this.readStatusByQueryParam(params.wptStatus, WptStatus, this.WPT_STATUS_GROUPS);
          this.filter.description = params.description ? decodeURIComponent(params.description) : '';
          this.getJobResults(jobId);
        }
      }
    }).unsubscribe();
  }

  private writeQueryParams(): void {
    const params: Params = {
      job: this.selectedJob.id,
      // from: encodeURIComponent(this.filter.from),
      // to: encodeURIComponent(this.filter.to),
      testAgent: this.filter.testAgent !== '' ? encodeURIComponent(this.filter.testAgent) : null,
      status: this.writeStatusAsQueryParam(this.filter.jobResultStatus, JobResultStatus),
      wptStatus: this.writeStatusAsQueryParam(this.filter.wptStatus, WptStatus),
      description: this.filter.description !== '' ? encodeURIComponent(this.filter.description) : null
    };

    this.router.navigate([], {
      queryParams: params,
      replaceUrl: true
    });
  }

  private getJobResults(jobId: number): void {
    this.dataService.getJobResults(jobId)
      .subscribe((jobResults: JobResult[]) => {
        this.jobResults = jobResults;
        this.filteredJobResults = jobResults;
        this.setFilter();
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
      if (typeof termOrTermList === 'string') {
        return status.toLowerCase().includes(termOrTermList.toLowerCase());
      }
      if (typeof termOrTermList === 'object' && termOrTermList['children'] && Array.isArray(termOrTermList['children'])) {
        return termOrTermList['children'].find((term: string) => status.toLowerCase().includes(term.toLowerCase()));
      }
      return false;
    });
  }

  private isWptNotTerminated(wptStatus: string): boolean {
    return this.WPT_STATUS_GROUPS.GROUP_NOT_TERMINATED.includes(wptStatus);
  }

  private isWptSuccessful(wptStatus: string): boolean {
    return this.WPT_STATUS_GROUPS.GROUP_SUCCESS.includes(wptStatus);
  }

  private hasWptFailed(wptStatus: string): boolean {
    return this.WPT_STATUS_GROUPS.GROUP_FAILED.includes(wptStatus);
  }

  private writeStatusAsQueryParam(statusList: (string | object)[], enumeration: any): string[] {
    if (statusList.length > 0) {
      return statusList.map(status => {
        if (typeof status === 'string') {
          return Object.keys(enumeration).find(key => enumeration[key] === status).toLowerCase();
        }
        if (typeof status === 'object' && status['label'] && typeof status['label'] === 'string') {
          return Object.keys(StatusGroup).find(key =>
              this.translateService.instant(StatusGroup[key]) === status['label']).toLowerCase();
        }
      });
    }
    return null;
  }

  private readStatusByQueryParam(statusParam: (string | string[]),
                                 enumeration: any,
                                 statusGroupType: {[key: string]: string []}
                                 ): (string | object)[] {
    if (!statusParam) {
      return [];
    }
    if (typeof statusParam === 'string') {
      statusParam = statusParam.toUpperCase();
      if (Object.keys(StatusGroup).find(key => key === statusParam)) {
        return [].concat({
          label: this.translateService.instant(StatusGroup[statusParam]),
          children: statusGroupType[statusParam]
        });
      }
      return [].concat(enumeration[statusParam]);
    }
    if (Array.isArray(statusParam)) {
      return statusParam.map((status: string) => {
        status = status.toUpperCase();
        if (Object.keys(StatusGroup).find(key => key === status)) {
          return {
            label: this.translateService.instant(StatusGroup[status]),
            children: statusGroupType[status]
          };
        }
        return enumeration[status];
      });
    }
  }
}
