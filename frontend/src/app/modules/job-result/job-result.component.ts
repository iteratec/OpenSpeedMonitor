import {Component, OnInit} from '@angular/core';
import {JobResultDataService} from './services/job-result-data.service';
import {JobResult} from './models/job-result.model';

@Component({
  selector: 'osm-job-result',
  templateUrl: './job-result.component.html',
  styleUrls: ['./job-result.component.scss']
})
export class JobResultComponent implements OnInit {

  jobResults: JobResult[] = [];
  currentSortingRule: { [key: string]: string } = {column: 'date', direction: 'desc'};

  constructor(private dataService: JobResultDataService) {
  }

  ngOnInit() {
    this.getJobResults();
  }

  getJobResults(): void {
    this.dataService.getJobResults(1065)
      .subscribe((jobResults: JobResult[]) => this.jobResults = jobResults);
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
}
