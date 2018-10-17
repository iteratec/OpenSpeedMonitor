import {Component, Input} from '@angular/core';
import {LocationInfoDTO} from "../../models/LocationInfoDTO";
import {ReplaySubject} from "rxjs/index";
import {ServerInfo} from "../../services/queue-dashboard.service";

@Component({
  selector: 'osm-queue-list',
  templateUrl: './location-info-list.component.html',
  styleUrls: ['./location-info-list.component.scss']
})
export class LocationInfoListComponent {

  @Input() wptServerID;

  locationInfo$ : ReplaySubject<LocationInfoDTO[]> = new ReplaySubject(1);

  label_id = "ID";
  label_agents = "Agenten";
  label_job_queue = "Job-Queue (WPT-Server)";
  label_osm_jobs = "OSM-Jobs";
  label_pending = "Pending";
  label_running = "Running";
  label_last_hour = "Letzte Stunde";
  label_next_hour = "Nächste Stunde";
  label_errors = "Fehler";
  label_jobs = "Jobs";
  label_events = "Messschritte";
  label_last_date = "Stand";
  label_status = "Status";
  label_started = "Gestartet";

  @Input()
  set serverInfo(value: ServerInfo )
  {
    if(value != null){
      let data = value[this.wptServerID];
      this.locationInfo$.next( data );
    }
  }

  parseDate(date: string)
  {
    return Date.parse(date);
  }

  togglePendingRunningJobs(arrowIcon: HTMLElement, row: HTMLTableRowElement)
  {
    if(row.nextElementSibling.className == "jobRow"){
      let nextrow = row.nextElementSibling as HTMLElement;
      if(nextrow.style.display == "table-row"){
        nextrow.style.display = "none";
        this.toggleArrow(arrowIcon, true);
      }
      else{
        nextrow.style.display = "table-row";
        this.toggleArrow(arrowIcon, false);
      }
    }
  }

  private toggleArrow(arrowIcon: HTMLElement, down: boolean) {
    arrowIcon.classList.toggle("fa-chevron-down", down);
    arrowIcon.classList.toggle("fa-chevron-up", !down);
  }
}
