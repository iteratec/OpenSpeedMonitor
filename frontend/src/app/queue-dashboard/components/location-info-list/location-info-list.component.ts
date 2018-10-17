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

  label_id = "frontend.de.iteratec.osm.queueDashboard.location-info-list.ID.label";
  label_agents = "frontend.de.iteratec.osm.queueDashboard.location-info-list.agents.label";
  label_job_queue = "frontend.de.iteratec.osm.queueDashboard.location-info-list.jobQueue.label";
  label_osm_jobs = "frontend.de.iteratec.osm.queueDashboard.location-info-list.osmJobs.label";
  label_pending = "frontend.de.iteratec.osm.queueDashboard.location-info-list.pending.label";
  label_running = "frontend.de.iteratec.osm.queueDashboard.location-info-list.running.label";
  label_last_hour = "frontend.de.iteratec.osm.queueDashboard.location-info-list.lastHour.label";
  label_next_hour = "frontend.de.iteratec.osm.queueDashboard.location-info-list.nextHour.label";
  label_errors = "frontend.de.iteratec.osm.queueDashboard.location-info-list.errors.label";
  label_jobs = "frontend.de.iteratec.osm.queueDashboard.location-info-list.jobs.label";
  label_events = "frontend.de.iteratec.osm.queueDashboard.location-info-list.events.label";
  label_last_date = "frontend.de.iteratec.osm.queueDashboard.location-info-list.lastDate.label";
  label_status = "frontend.de.iteratec.osm.queueDashboard.location-info-list.status.label";
  label_started = "frontend.de.iteratec.osm.queueDashboard.location-info-list.started.label";

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
