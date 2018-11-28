import {Component, Input} from '@angular/core';
import {NgxSmartModalService} from "ngx-smart-modal";
import {GraphiteServer} from "../../../models/graphite-server.model";
import {ApplicationService} from "../../../../../services/application.service";
import {Application} from "../../../../../models/application.model";
import {GrailsBridgeService} from "../../../../../services/grails-bridge.service";

@Component({
  selector: 'osm-graphite-integration',
  templateUrl: './graphite-integration.component.html',
  styleUrls: ['./graphite-integration.component.scss']
})
export class GraphiteIntegrationComponent {

  @Input() selectedApplication: Application;

  jobHealthGraphiteServers: GraphiteServer[];
  availableGraphiteServers: GraphiteServer[];

  graphiteServersToAdd: GraphiteServer[] = [];
  graphiteServersToRemove: GraphiteServer[] = [];

  selectedGraphiteServer: GraphiteServer;

  constructor(public ngxSmartModalService: NgxSmartModalService, private applicationService: ApplicationService, private grailsBridgeService: GrailsBridgeService) {
    this.applicationService.jobHealthGraphiteServers$.subscribe(value => {this.jobHealthGraphiteServers = value});
    this.applicationService.availableGraphiteServers$.subscribe(value => {this.availableGraphiteServers = value});
  }

  save(): void {
    if (this.grailsBridgeService.globalOsmNamespace.user.loggedIn) {
      if (this.graphiteServersToAdd.length) {
        this.applicationService.saveJobHealthGraphiteServers(this.selectedApplication, this.graphiteServersToAdd);
      }
      if (this.graphiteServersToRemove.length) {
        this.applicationService.removeJobHealthGraphiteServers(this.selectedApplication, this.graphiteServersToRemove);
      }
      this.ngxSmartModalService.close('graphiteIntegrationModal');
    } else {
      window.location.href = '/login/auth';
    }
  }

  cancel(): void {
    this.applicationService.loadAvailableGraphiteServers(this.selectedApplication);
    this.applicationService.loadActiveJobHealthGraphiteServers(this.selectedApplication);
    this.graphiteServersToAdd = [];
    this.graphiteServersToRemove = [];
    this.selectedGraphiteServer = null;
  }

  add(graphiteServer: GraphiteServer): void {
    this.graphiteServersToAdd.push(graphiteServer);
    this.availableGraphiteServers = this.availableGraphiteServers.filter(value => value !== graphiteServer);
    this.jobHealthGraphiteServers.push(graphiteServer);
  }

  remove(graphiteServer: GraphiteServer): void {
    if (this.graphiteServersToAdd.includes(graphiteServer)) {
      this.graphiteServersToAdd = this.graphiteServersToAdd.filter(value => value !== graphiteServer);
      this.jobHealthGraphiteServers = this.jobHealthGraphiteServers.filter(value => value !== graphiteServer);
      this.availableGraphiteServers.push(graphiteServer);
    } else {
      this.graphiteServersToRemove.push(graphiteServer);
      this.jobHealthGraphiteServers = this.jobHealthGraphiteServers.filter(value => value !== graphiteServer);
      this.availableGraphiteServers.push(graphiteServer);
    }
    this.selectedGraphiteServer = null;
  }

  toggleSelectedGraphiteServer(graphiteServer: GraphiteServer): void {
    if (this.selectedGraphiteServer === graphiteServer) {
      this.selectedGraphiteServer = null;
    } else {
      this.selectedGraphiteServer = graphiteServer;
    }
  }

}
