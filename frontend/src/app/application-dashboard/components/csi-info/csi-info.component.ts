import {Component, Input, OnInit, OnChanges, SimpleChanges} from '@angular/core';
import {ApplicationCsiListDTO} from "../../models/csi-list.model";

@Component({
  selector: 'osm-csi-info',
  templateUrl: './csi-info.component.html',
  styleUrls: ['./csi-info.component.scss']
})
export class CsiInfoComponent implements OnChanges {

  @Input() csiData: ApplicationCsiListDTO;

  infoText: string;

  ngOnChanges(changes: SimpleChanges): void {
    this.infoText = this.setText()
  }

  private setText () {
    if (!this.csiData.hasCsiConfiguration) {
      // Case 2: No CSI configuration for application
      return 'The calculation of the customer satisfaction index (CSI) is not configured for this application.'
    }
    if (this.csiData.hasJobResults) {
      if (this.csiData.hasInvalidJobResults) {
        // Case 4: Measurements have errors
        return 'The measurements are not working properly.'
      }
      // Case 3: CSI config and job results but no csi value
      return 'The configuration of the customer satisfaction index (CSI) does not produce a CSI value for this application or there are no new measurements since the CSI configuration has been updated.'
    }
    // Case 1: Application has not been measured
    return 'This application has not been measured yet.'
  }

}
