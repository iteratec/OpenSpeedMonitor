import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ApplicationCsiListDTO} from "../../models/csi-list.model";
import {ApplicationDTO} from "../../models/application.model";
import {ApplicationDashboardService} from "../../services/application-dashboard.service";
import {GrailsBridgeService} from "../../../shared/services/grails-bridge.service";

@Component({
  selector: 'osm-queue-row',
  templateUrl: './location-row.component.html',
  styleUrls: ['./location-row.component.scss']
})
export class LocationRow{

  //@Input() csiData: ApplicationCsiListDTO;
  //@Input() selectedApplication: ApplicationDTO;

  constructor(private applicationDashboardService: ApplicationDashboardService, private grailsBridgeService: GrailsBridgeService) {
  }

}
