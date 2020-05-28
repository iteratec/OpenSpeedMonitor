import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {QueueDashboardService, ServerInfo} from './services/queue-dashboard.service';
import {WptServerDTO} from './models/WptServerDTO';
import {TitleService} from '../../services/title.service';

@Component({
  selector: 'osm-app-queue-dashboard',
  templateUrl: './queue-dashboard.component.html',
  styleUrls: ['./queue-dashboard.component.scss']
})
export class QueueDashboardComponent {

  wptServer$: Observable<Array<WptServerDTO>>;
  serverInfo$: Observable<ServerInfo>;
  private queueService: QueueDashboardService;

  constructor(queueService: QueueDashboardService, private titleService: TitleService) {
    this.titleService.setTitle('frontend.de.iteratec.osm.queueDashboard.title');
    this.queueService = queueService;
    this.wptServer$ = this.queueService.activeServers$;
    this.serverInfo$ = this.queueService.serverInfo$;
    this.queueService.getActiveWptServer();
  }

  loadQueueContent(id: number) {
    this.queueService.getInfoTableForWptServer(id);
  }
}
