import {Component, Input, OnInit, ViewEncapsulation} from '@angular/core';
import {ResultSelectionService} from "../../../../services/result-selection.service";
import {NgxSmartModalService} from "ngx-smart-modal";
import {ApplicationService} from "../../../../services/application.service";
import {ReplaySubject, Subject} from "rxjs";
import {ResponseWithLoadingState} from "../../../../models/response-with-loading-state.model";
import {PerformanceAspect} from "../../../../models/perfomance-aspect.model";

@Component({
  selector: 'osm-performance-aspect-management',
  templateUrl: './performance-aspect-management.component.html',
  styleUrls: ['./performance-aspect-management.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class PerformanceAspectManagementComponent implements OnInit {
  @Input() pageId: number;
  @Input() pageName: string;
  performanceAspects$: Subject<ResponseWithLoadingState<PerformanceAspect>[]>;

  constructor(private ngxSmartModalService: NgxSmartModalService, private measurandsService: ResultSelectionService, private applicationService: ApplicationService) {
    this.performanceAspects$ = this.applicationService.performanceAspectForPage$
  }

  ngOnInit() {
  }

  initDialog(){
    this.measurandsService.updatePages([{id: this.pageId, name: "does-not-matter"}]);
    this.applicationService.updatePage({id: this.pageId, name: "does-not-matter"});
    this.ngxSmartModalService.open('preformanceAspectMgmtModal');
  }

  cancel(){

  }

}
