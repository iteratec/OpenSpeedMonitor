import {EventResultSeriesDTO} from './event-result-series.model';
import {EventDTO} from './event.model';

export interface EventResultDataDTO {
  series: EventResultSeriesDTO[];
  events: EventDTO[];
}

export class EventResultData implements EventResultDataDTO {
  series: EventResultSeriesDTO[];
  events: EventDTO[];

  constructor() {
    this.series = [];
    this.events = [];
  }
}
