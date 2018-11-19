import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {NgxSmartModalService} from "ngx-smart-modal";
import {JobHealthGraphiteServers} from "../../../models/job-health-graphite-servers.model";

@Component({
  selector: 'osm-graphite-integration',
  templateUrl: './graphite-integration.component.html',
  styleUrls: ['./graphite-integration.component.scss']
})
export class GraphiteIntegrationComponent implements OnChanges {

  @Input() jobHealthGraphiteServers: JobHealthGraphiteServers;

  constructor(public ngxSmartModalService: NgxSmartModalService) { }

  ngOnChanges(changes: SimpleChanges): void {
    setTimeout(() => {
      this.ngxSmartModalService.resetModalData('graphiteIntegrationModal');
      this.ngxSmartModalService.setModalData(this.jobHealthGraphiteServers, 'graphiteIntegrationModal');
    });
  }
}
