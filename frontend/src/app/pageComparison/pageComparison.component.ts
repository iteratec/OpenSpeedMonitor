import {Component, OnInit} from '@angular/core';
import {ResultSelectionService} from "./service/resultSelection.service";
import {JobGroupToPagesMapping} from "./service/JobGroupToPagesMapping";
import {PageComparisonSelection} from "./PageComparisonSelection";

@Component({
  selector: 'page-comparison',
  templateUrl: './pageComparison.component.html'
  // styleUrls: ['./.component.css']
})
export class PageComparisonComponent implements OnInit {
  jobGroupMappings:JobGroupToPagesMapping[] = [];
  pageComparisonSelections: PageComparisonSelection[] = [];

  constructor(private resultSelectionService:ResultSelectionService) {
  }

  ngOnInit() {
    this.getJobGroups();
    this.addComparison();
  }

  addComparison(){
    this.pageComparisonSelections.push(new PageComparisonSelection())
    document.getElementById("addComparison2").dispatchEvent(new Event('test'));
  }

  getJobGroups() {
    this.resultSelectionService.getJobGroupToPagesMap("","").subscribe((map: any[]) => {
      this.jobGroupMappings.length = 0;
      for (let key in map){
        this.jobGroupMappings.push(JobGroupToPagesMapping.createFromJSON(key,map[key]))
      }
    })
  }
}
