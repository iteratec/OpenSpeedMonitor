import {Component, Input} from '@angular/core';
import {LocationInfoDTO} from "../../models/LocationInfoDTO";
import {ServerInfo} from "../../services/queue-dashboard.service";
import {OsmLangService} from "../../../../services/osm-lang.service";
import {DatePipe} from "@angular/common";
import {local} from "d3-selection";

@Component({
  selector: 'osm-queue-list',
  templateUrl: './location-info-list.component.html',
  styleUrls: ['./location-info-list.component.scss']
})
export class LocationInfoListComponent {

  @Input() wptServerID;

  locationInfo : LocationInfoDTO[] = [];

  constructor(private langservice: OsmLangService) {}

  @Input()
  set serverInfo(value: ServerInfo ) {
    if(value != null){
      this.locationInfo = value[this.wptServerID];
    }
  }

  parseDate(date: string) {
    let locale = this.langservice.getOsmLang();
    let datepipe = new DatePipe(locale ? locale : "en");
    return datepipe.transform(date, "short");
  }
}
