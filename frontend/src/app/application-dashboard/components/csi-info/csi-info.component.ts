import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ApplicationCsiListDTO} from "../../models/csi-list.model";
import {ApplicationDTO} from "../../models/application.model";
import {ApplicationDashboardService} from "../../services/application-dashboard.service";

@Component({
  selector: 'osm-csi-info',
  templateUrl: './csi-info.component.html',
  styleUrls: ['./csi-info.component.scss']
})
export class CsiInfoComponent implements OnChanges {

  @Input() csiData: ApplicationCsiListDTO;
  @Input() selectedApplication: ApplicationDTO;

  errorCase: number;
  infoText: string;
  buttonText: string;
  iconClass: string;

  constructor(private applicationDashboardService: ApplicationDashboardService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.infoText = this.setInformation();
  }

  private setInformation () {
    if (!this.csiData.hasCsiConfiguration) {
      // Case 2: No CSI configuration for application
      this.errorCase = 2;
      this.iconClass = 'icon-info fas fa-info-circle';
      this.buttonText = 'frontend.de.iteratec.osm.applicationDashboard.csiInfo.button.noCsiConfig';
      return 'frontend.de.iteratec.osm.applicationDashboard.csiInfo.noCsiConfig';
    }
    if (this.csiData.hasJobResults) {
      if (this.csiData.hasInvalidJobResults) {
        // Case 4: Measurements have errors
        this.errorCase = 4;
        this.iconClass = 'icon-warning fas fa-exclamation-triangle';
        this.buttonText = 'frontend.de.iteratec.osm.applicationDashboard.csiInfo.button.invalidMeasurement';
        return 'frontend.de.iteratec.osm.applicationDashboard.csiInfo.invalidMeasurement';
      }
      // Case 3: CSI config and job results but no csi value
      this.errorCase = 3;
      this.iconClass = 'icon-warning fas fa-exclamation-triangle';
      this.buttonText = 'frontend.de.iteratec.osm.applicationDashboard.csiInfo.button.noCsiValue';
      return 'frontend.de.iteratec.osm.applicationDashboard.csiInfo.noCsiValue';
    }
    // Case 1: Application has not been measured
    this.errorCase = 1;
    this.iconClass = 'icon-info fas fa-info-circle';
    return 'frontend.de.iteratec.osm.applicationDashboard.csiInfo.notMeasured';
  }

  createCsiConfiguration () {
    this.applicationDashboardService.createCsiConfiguration(this.selectedApplication);
  }

}
