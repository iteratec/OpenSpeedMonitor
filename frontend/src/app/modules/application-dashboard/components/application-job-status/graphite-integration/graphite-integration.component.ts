import {Component, Input} from '@angular/core';
import {NgxSmartModalService} from "ngx-smart-modal";
import {JobHealthGraphiteServers} from "../../../models/job-health-graphite-servers.model";
import {GraphiteServer} from "../../../models/graphite-server.model";
import {ApplicationService} from "../../../../../services/application.service";
import {Application} from "../../../../../models/application.model";
import {Observable} from "rxjs";
import {GrailsBridgeService} from "../../../../../services/grails-bridge.service";

@Component({
  selector: 'osm-graphite-integration',
  templateUrl: './graphite-integration.component.html',
  styleUrls: ['./graphite-integration.component.scss']
})
export class GraphiteIntegrationComponent {

  @Input() selectedApplication: Application;

  jobHealthGraphiteServers$: Observable<JobHealthGraphiteServers>;
  availableGraphiteServers$: Observable<JobHealthGraphiteServers>;

  constructor(public ngxSmartModalService: NgxSmartModalService, private applicationService: ApplicationService, private grailsBridgeService: GrailsBridgeService) {
    this.jobHealthGraphiteServers$ = this.applicationService.jobHealthGraphiteServers$;
    this.availableGraphiteServers$ = this.applicationService.availableGraphiteServers$;
  }

  addGraphiteServer(graphiteServer: GraphiteServer): void {
    if (this.grailsBridgeService.globalOsmNamespace.user.loggedIn) {
      this.applicationService.addJobHealthGraphiteServer(this.selectedApplication, graphiteServer);
    } else {
      window.location.href = '/login/auth';
    }
  }

  removeGraphiteServer(graphiteServer: GraphiteServer) {
    if (this.grailsBridgeService.globalOsmNamespace.user.loggedIn) {
      this.applicationService.removeJobHealthGraphiteServer(this.selectedApplication, graphiteServer)
    }
  }

}
