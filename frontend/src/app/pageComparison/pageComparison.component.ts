import {Component, OnInit} from '@angular/core';
import {ResultSelectionService} from "./service/resultSelection.service";
import {JobGroupToPagesMapping} from "./service/JobGroupToPagesMapping";
import {PageComparisonSelection} from "./PageComparisonSelection";

@Component({
  selector: 'page-comparison',
  templateUrl: './pageComparison.component.html'
})
export class PageComparisonComponent implements OnInit {
  jobGroupMappings: JobGroupToPagesMapping[] = [];
  pageComparisonSelections: PageComparisonSelection[] = [];

  constructor(private resultSelectionService: ResultSelectionService) {
  }

  ngOnInit() {
    this.addComparison();
    this.registerTimeFrameChangeEvent();
  }

  registerTimeFrameChangeEvent() {
    document.getElementById("select-interval-timeframe-card").addEventListener("timeFrameChanged", (event: any) => {
      this.getJobGroups(event.detail[0].toISOString(), event.detail[1].toISOString());
    })
  }

  addComparison() {
    this.pageComparisonSelections.push(new PageComparisonSelection())
  }

  getJobGroups(from: string, to: string) {
    this.resultSelectionService.getJobGroupToPagesMap(from, to).subscribe((map: any[]) => {
      this.jobGroupMappings.length = 0;
      for (let key in map) {
        this.jobGroupMappings.push(JobGroupToPagesMapping.createFromJSON(key, map[key]))
      }
    })
  }
}
