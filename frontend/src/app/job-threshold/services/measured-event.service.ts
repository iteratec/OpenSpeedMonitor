import {Injectable} from '@angular/core';
import {MeasuredEvent} from '../models/measured-event.model';
import {ThresholdRestService} from './threshold-rest.service';
import {ReplaySubject} from 'rxjs';

@Injectable()
export class MeasuredEventService {

  measuredEventList: MeasuredEvent[];
  public measuredEvents$ = new ReplaySubject<MeasuredEvent[]>(1);

  constructor(private thresholdRestService: ThresholdRestService) {
  }

  fetchEvents(scriptId, jobId) {
    this.thresholdRestService.getMeasuredEvents(scriptId, jobId).subscribe(this.measuredEvents$);
    this.measuredEvents$.subscribe((measuredEventsFromServer: MeasuredEvent[]) => {
      this.measuredEventList = measuredEventsFromServer;
    });
  }
}
