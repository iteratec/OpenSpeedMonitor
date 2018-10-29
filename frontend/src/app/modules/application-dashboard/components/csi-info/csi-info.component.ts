import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ApplicationCsi} from "../../../../models/csi-list.model";
import {Application} from "../../../../models/application.model";
import {ApplicationService} from "../../../../services/application.service";
import {GrailsBridgeService} from "../../../../services/grails-bridge.service";

@Component({
  selector: 'osm-csi-info',
  templateUrl: './csi-info.component.html',
  styleUrls: ['./csi-info.component.scss']
})
export class CsiInfoComponent implements OnChanges {

  @Input() csiData: ApplicationCsi;
  @Input() selectedApplication: Application;

  errorCase: string;
  infoText: string;
  iconClass: string;

  constructor(private applicationDashboardService: ApplicationService, private grailsBridgeService: GrailsBridgeService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.setInformation();
  }

  private setInformation(): void {
    if (this.csiData && !this.csiData.hasCsiConfiguration) {
      this.errorCase = 'noCsiConfig';
      this.infoText = 'frontend.de.iteratec.osm.applicationDashboard.csiInfo.noCsiConfig';
      this.iconClass = 'icon-info fas fa-info-circle';
      return
    }
    if (this.csiData && this.csiData.hasJobResults) {
      if (this.csiData.hasInvalidJobResults) {
        this.errorCase = 'invalidMeasurement';
        this.infoText = 'frontend.de.iteratec.osm.applicationDashboard.csiInfo.invalidMeasurement';
        this.iconClass = 'icon-warning fas fa-exclamation-triangle';
        return
      }
      this.errorCase = 'noCsiValue';
      this.iconClass = 'icon-warning fas fa-exclamation-triangle';
      this.infoText = 'frontend.de.iteratec.osm.applicationDashboard.csiInfo.noCsiValue';
      return
    }
    this.errorCase = 'notMeasured';
    this.infoText = 'frontend.de.iteratec.osm.applicationDashboard.csiInfo.notMeasured';
    this.iconClass = 'icon-info fas fa-info-circle';
    return
  }

  createCsiConfiguration() {
    if (this.grailsBridgeService.globalOsmNamespace.user.loggedIn) {
      this.applicationDashboardService.createCsiConfiguration(this.selectedApplication);
    } else {
      window.location.href = '/login/auth';
    }
  }

}
