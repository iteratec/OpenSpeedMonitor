import {Component, Input, OnInit} from '@angular/core';
import {ResultSelectionService} from "../../../../services/result-selection.service";
import {NgxSmartModalService} from "ngx-smart-modal";

@Component({
  selector: 'osm-performance-aspect-management',
  templateUrl: './performance-aspect-management.component.html',
  styleUrls: ['./performance-aspect-management.component.scss']
})
export class PerformanceAspectManagementComponent implements OnInit {
  @Input() pageId: number;

  constructor(private ngxSmartModalService: NgxSmartModalService, private measurandsService: ResultSelectionService) { }

  ngOnInit() {

  }

  initDialog(){
    this.measurandsService.updatePages([{id: this.pageId, name: "does-not-matter"}]);
    this.ngxSmartModalService.open('preformanceAspectMgmtModal');
  }

  cancel(){

  }

}
