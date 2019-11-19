import {EventResultSeriesDTO} from './event-result-series.model';

export interface EventResultDataDTO {
  measurandGroups: any;
  series: EventResultSeriesDTO[];
}

export class EventResultData implements EventResultDataDTO {
  measurandGroups: any;
  series: EventResultSeriesDTO[];

  constructor() {
    this.measurandGroups = {};
    this.series = [];
  }
}
