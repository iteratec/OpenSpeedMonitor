import {Component, Input, OnInit, ViewEncapsulation} from '@angular/core';
import {ResultSelectionService} from "../../../result-selection/services/result-selection.service";
import {NgxSmartModalService} from "ngx-smart-modal";
import {ApplicationService} from "../../../../services/application.service";
import {Subject} from "rxjs";
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
  changedMetrics: Map<string, PerformanceAspect>;

  constructor(private ngxSmartModalService: NgxSmartModalService, private measurandsService: ResultSelectionService, private applicationService: ApplicationService) {
    this.performanceAspects$ = this.applicationService.performanceAspectForPage$;
  }

  ngOnInit() {
    this.changedMetrics = new Map<string, PerformanceAspect>();
  }

  initDialog() {
    this.ngxSmartModalService.setModalData(this.pageName, "performanceAspectMgmtModal");
    this.measurandsService.updatePages([{id: this.pageId, name: this.pageName}]);
    this.applicationService.updatePage({id: this.pageId, name: this.pageName});
    this.ngxSmartModalService.open('performanceAspectMgmtModal');
  }

  resetModalData() {
    this.changedMetrics.clear();
    this.ngxSmartModalService.resetModalData('performanceAspectMgmtModal');
  }

  updatePerformanceAspect(performanceAspect: PerformanceAspect) {
    let key: string = performanceAspect.jobGroupId + "." + performanceAspect.pageId + "." + performanceAspect.performanceAspectType;
    this.changedMetrics.set(key, performanceAspect);
  }

  saveAndClose() {
    this.changedMetrics.forEach((performanceAspect: PerformanceAspect) => {
      this.applicationService.createOrUpdatePerformanceAspect(performanceAspect);
    });
    this.ngxSmartModalService.close('performanceAspectMgmtModal');
  }

}
