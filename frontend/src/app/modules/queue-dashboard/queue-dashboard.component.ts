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

  constructor( queueService: QueueDashboardService) {
    this.queueService = queueService;
    this.wptServer$ = this.queueService.activeServers$;
    this.serverInfo$ = this.queueService.serverInfo$;
    this.queueService.getActiveWptServer();
  }

  loadQueueContent( id: number){
    this.queueService.getInfoTableForWptServer(id);
  }
}
