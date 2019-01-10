import {Component, Input} from '@angular/core';
import {NgxSmartModalService} from "ngx-smart-modal";
import {GraphiteServer} from "../../../models/graphite-server.model";
import {ApplicationService} from "../../../../../services/application.service";
import {Application} from "../../../../../models/application.model";
import {GrailsBridgeService} from "../../../../../services/grails-bridge.service";
import {FormGroup, FormControl} from "@angular/forms";
import {switchMap} from "rxjs/operators";
import {Observable} from "rxjs";

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

  showCreateSection = false;
  createServerForm : FormGroup;

  constructor(public ngxSmartModalService: NgxSmartModalService, private applicationService: ApplicationService, private grailsBridgeService: GrailsBridgeService) {
    this.resetFormGroup();
    this.applicationService.jobHealthGraphiteServers$.subscribe(value => {this.jobHealthGraphiteServers = value});
    this.applicationService.availableGraphiteServers$.subscribe(value => {this.availableGraphiteServers = value});
  }

  save(): void {
    if (this.grailsBridgeService.globalOsmNamespace.user.loggedIn) {
      if (this.graphiteServersToRemove.length && this.graphiteServersToAdd.length) {
        this.applicationService.removeAndAddJobHealthGraphiteServers(this.selectedApplication, this.graphiteServersToAdd, this.graphiteServersToRemove)
      } else if (this.graphiteServersToRemove.length) {
        this.applicationService.removeJobHealthGraphiteServers(this.selectedApplication, this.graphiteServersToRemove);
      } else if (this.graphiteServersToAdd.length) {
        this.applicationService.saveJobHealthGraphiteServers(this.selectedApplication, this.graphiteServersToAdd);
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
      this.showCreateSection = false;
    }
  }

  showCreationSection(): void {
    this.showCreateSection = true;
    this.selectedGraphiteServer = null;
  }

  cancelCreation(): void {
    this.showCreateSection = false;
    this.resetFormGroup();
  }

  create(): void {
    if(!this.grailsBridgeService.globalOsmNamespace.user.loggedIn) {
      window.location.href = '/login/auth';
    }
    const observable = this.applicationService.createGraphiteServer( {
      address : this.createServerForm.get('address').value,
      id : null,
      port : this.createServerForm.get('port').value,
      prefix : this.createServerForm.get('prefix').value,
      protocol : this.createServerForm.get('protocol').value,
      webAppAddress : this.createServerForm.get('webAppAddress').value
    });
    if(observable){
      this.resetFormGroup();
      this.addNewServer(observable);
    }
  }

  addNewServer( observable: Observable<Map<String, any>> ) : void {
    observable.subscribe(next => {
      if (next != null && next["success"] && next["id"] != null) {
        this.applicationService.selectedApplication$.pipe(
          switchMap((application: Application) => this.applicationService.updateAvailableGraphiteServers(application))
        ).subscribe(server => {
          this.applicationService.availableGraphiteServers$.next(server);
          this.selectNewServer(server, next["id"]);
        });
      }
    });
  }

  private selectNewServer(server: GraphiteServer[], id: number, ): void {
    if (server != null) {
      const target = server.find(value => value.id == id);
      this.add(target);
      this.toggleSelectedGraphiteServer(target);
    }
  }

  private resetFormGroup(): void {
    this.createServerForm = new FormGroup({
      address: new FormControl(''),
      port: new FormControl(2003),
      protocol: new FormControl('TCP'),
      webAppAddress: new FormControl(''),
      prefix: new FormControl('jobstatus')
    });
  }
}
