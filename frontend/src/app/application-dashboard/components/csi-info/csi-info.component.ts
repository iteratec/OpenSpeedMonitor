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
      return 'The calculation of the customer satisfaction index (CSI) is not configured for this application.'
    }
    if (this.csiData.hasJobResults) {
      if (this.csiData.hasInvalidJobResults) {
        // Case 4: Measurements have errors
        this.errorCase = 4;
        this.iconClass = 'icon-warning fas fa-exclamation-triangle';
        return 'The measurements are not working properly.'
      }
      // Case 3: CSI config and job results but no csi value
      this.errorCase = 3;
      this.iconClass = 'icon-warning fas fa-exclamation-triangle';
      return 'The configuration of the customer satisfaction index (CSI) does not produce a CSI value for this application or there are no new measurements since the CSI configuration has been updated.'
    }
    // Case 1: Application has not been measured
    this.iconClass = 'icon-info fas fa-info-circle';
    return 'This application has not been measured yet.'
  }

  private createCsiConfiguration () {
    this.applicationDashboardService.createCsiConfiguration(this.selectedApplication);
  }

}
