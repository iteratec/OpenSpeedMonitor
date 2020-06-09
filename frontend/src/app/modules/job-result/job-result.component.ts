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

  sort(event: any) {
  }

}
