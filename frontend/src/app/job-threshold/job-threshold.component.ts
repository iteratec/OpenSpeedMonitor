import { Component, OnInit, ElementRef} from '@angular/core';
import { ThresholdRestService } from './service/rest/threshold-rest.service';

@Component({
  selector: 'app-job-threshold',
  templateUrl: './job-threshold.component.html',
  styleUrls: ['./job-threshold.component.css']
})
export class JobThresholdComponent implements OnInit {


    /*var activeMeasuredEvents: [];*/
  private jobId : "";
  private scriptId : number;
  private measurands : any[];
  private measuredEvents : any[];



/*
    copiedMeasuredEvents: [];*/


  constructor(private thresholdRestService: ThresholdRestService,
              elm: ElementRef) {
    this.jobId = elm.nativeElement.getAttribute('data-job-id');
    this.scriptId = elm.nativeElement.getAttribute('data-job-scriptId');
  }

  ngOnInit() {

    /*this.jobId = this.$el.attributes['jobId'].value;*/
    /*this.scriptId = this.$el.attributes['scriptId'].value;*/
    console.log("ngOnInit measurands: " + this.measurands);
    console.log("ngOnInit jobId: " + this.jobId);
    console.log("ngOnInit scriptId: " + this.scriptId);
    this.fetchData();
  }

  fetchData() {
    this.thresholdRestService.getMeasurands().subscribe((measurands: any[]) => {
      console.log("measurands: " + JSON.stringify(measurands ));
    });

    this.thresholdRestService.getMeasuredEvents(this.scriptId).subscribe((measuredEvents: any[]) => {
      console.log("measuredEvents: " + JSON.stringify(measuredEvents ));
    });



  }



}
