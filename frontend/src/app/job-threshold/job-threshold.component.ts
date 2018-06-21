import { Component, OnInit } from '@angular/core';
import { ThresholdRestService } from './service/rest/threshold-rest.service';

@Component({
  selector: 'app-job-threshold',
  templateUrl: './job-threshold.component.html',
  styleUrls: ['./job-threshold.component.css']
})
export class JobThresholdComponent implements OnInit {


    /*var activeMeasuredEvents: [];
    var measuredEvents: [];*/

    private measurands : any[];

/*   jobId: "";
    scriptId: "";
    copiedMeasuredEvents: [];*/


  constructor(private thresholdRestService: ThresholdRestService) { }

  ngOnInit() {
    console.log("ngOnInit measurands: " + this.measurands);;
    this.fetchData();
  }

  fetchData() {
    this.thresholdRestService.fetchData().subscribe((measurands: any[]) => {
      console.log("measurands: " + measurands);
    })
  }



}
