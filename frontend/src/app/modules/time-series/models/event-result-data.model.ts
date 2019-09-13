import {EventResultSeriesDTO} from './event-result-series.model';

export interface EventResultDataDTO {
  series: EventResultSeriesDTO[];
}

export class EventResultData implements EventResultDataDTO {
  series: EventResultSeriesDTO[];

  constructor() {
    this.series = [];
  }
}
