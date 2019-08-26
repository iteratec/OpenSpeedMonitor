import {EventResultPointDTO} from './event-result-point.model';

export interface EventResultSeriesDTO {
  identifier: string;
  jobGroup: string;
  measuredEvent: string;
  data: EventResultPointDTO[];
}

export class EventResultSeries implements EventResultSeriesDTO {
  identifier: string;
  jobGroup: string;
  measuredEvent: string;
  data: EventResultPointDTO[];

  constructor(dto: EventResultSeriesDTO) {
    this.identifier = dto.identifier;
    this.jobGroup = dto.jobGroup;
    this.measuredEvent = dto.measuredEvent;
    this.data = dto.data;
  }
}
