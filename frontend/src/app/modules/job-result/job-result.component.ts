import {Component, OnInit} from '@angular/core';
import {JobResultDataService} from './services/job-result-data.service';
import {JobResult} from './models/job-result.model';
import {Job} from './models/job.model';
import {ActivatedRoute, Params, Router} from '@angular/router';

@Component({
  selector: 'osm-job-result',
  templateUrl: './job-result.component.html',
  styleUrls: ['./job-result.component.scss']
})
export class JobResultComponent implements OnInit {

  jobs: Job[] = [];
  jobResults: JobResult[] = [];

  selectedJob: Job = null;
  currentSortingRule: { [key: string]: string } = {column: 'date', direction: 'desc'};

  constructor(private dataService: JobResultDataService, private route: ActivatedRoute, private router: Router) {
  }

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
    }
  }

  isTestSuccessful(test: JobResult): boolean {
    return test.jobResultStatus === 'Finished' && test.wptStatus === 'COMPLETED (200)' && test.description === 'Ok';
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

    this.jobResults.sort((a: JobResult, b: JobResult) =>
      this.compareJobResults(a, b, this.currentSortingRule.column, this.currentSortingRule.direction)
    );
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
      .subscribe((jobResults: JobResult[]) => this.jobResults = jobResults);
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
}
