import {
  Component
} from '@angular/core';
import {Observable} from 'rxjs';
import {QueueDashboardService} from "./services/queue-dashboard.service";
import {WptServerDTO} from "./models/WptServerDTO";
import {ServerInfo} from "./services/queue-dashboard.service";

@Component({
  selector: 'app-queue-dashboard',
  templateUrl: './queue-dashboard.component.html',
  styleUrls: ['./queue-dashboard.component.scss']
})
export class QueueDashboardComponent {

  private queueService: QueueDashboardService;
  wptServer$: Observable<Array<WptServerDTO>>;
  serverInfo$: Observable<ServerInfo>;

  button_label = "Informationen Laden"

  constructor( queueService: QueueDashboardService) {
    this.queueService = queueService;
    this.wptServer$ = this.queueService.activeServer$;
    this.serverInfo$ = this.queueService.serverInfo$;
    this.queueService.getActiveWptServer();
  }

  loadQueueContent( id: number, button: HTMLButtonElement, arrow: HTMLSpanElement, table: HTMLElement){
    button.remove();
    arrow.style.display = "block";
    table.style.display = "block";
    this.queueService.getInfoTableForWptServer(id);
  }

  toggleTableVisibility( id: number, arrow: HTMLElement, table: HTMLElement) {
    if(table.style.display == "block") {
      arrow.classList.toggle("fa-chevron-down", true);
      arrow.classList.toggle("fa-chevron-up", false);
      table.style.display = "none";
    }
    else {
      arrow.classList.toggle("fa-chevron-down", false);
      arrow.classList.toggle("fa-chevron-up", true);
      table.style.display = "block";
    }
  }
}
