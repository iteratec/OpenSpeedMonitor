import {Component, OnInit} from '@angular/core';
import {JobResultDataService} from './services/job-result-data.service';
import {JobResult} from './models/job-result.model';
import {Job} from './models/job.model';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {JobResultStatus} from './models/job-result-status.enum';
import {WptStatus} from './models/wpt-status.enum';
import {JobResultFilter} from './models/job-result-filter.model';
import {StatusService} from './services/status.service';

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

  constructor(private dataService: JobResultDataService,
              public statusService: StatusService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  jobResultStatusGroupByFn = (jobResultStatus: string): string => this.statusService.getJobResultStatusGroupName(jobResultStatus);

  wptStatusGroupByFn = (wptStatus: string): string => this.statusService.getWptStatusGroupName(wptStatus);

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

  applyFilter(): void {
    this.writeQueryParams();
    this.filteredJobResults = this.jobResults.filter((jobResult: JobResult) => {
      if (this.arePropertiesThatAreFilteredEmpty(jobResult)) {
        return false;
      }
      return this.isJobResultIncludingTerms(jobResult);
    });
  }

  clearTextFilter(column: string): void {
    this.filter[column] = '';
    this.applyFilter();
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
          this.filter.jobResultStatus = this.statusService.readStatusByQueryParam(
            params.status, JobResultStatus, this.statusService.JOB_RESULT_STATUS_GROUPS
          );
          this.filter.wptStatus = this.statusService.readStatusByQueryParam(
            params.wptStatus, WptStatus, this.statusService.WPT_STATUS_GROUPS
          );
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
      status: this.statusService.writeStatusAsQueryParam(this.filter.jobResultStatus, JobResultStatus),
      wptStatus: this.statusService.writeStatusAsQueryParam(this.filter.wptStatus, WptStatus),
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
        this.applyFilter();
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
      if (!this.statusService.isStatusIncludingTerms(jobResult.jobResultStatus, this.filter.jobResultStatus)) {
        return false;
      }
    }
    if (jobResult.wptStatus && this.filter.wptStatus.length > 0) {
      if (!this.statusService.isStatusIncludingTerms(jobResult.wptStatus, this.filter.wptStatus)) {
        return false;
      }
    }
    if (jobResult.description && !jobResult.description.toLowerCase().includes(this.filter.description.toLowerCase())) {
      return false;
    }
    return true;
  }
}
