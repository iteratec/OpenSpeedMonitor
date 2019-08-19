import {TimeSeriesDataPointDTO} from './time-series-data-point.model';

export interface TimeSeriesDataDTO {
  identifier: string;
  jobGroup: string;
  measuredEvent: string;
  data: TimeSeriesDataPointDTO[];
}

export class TimeSeriesData implements TimeSeriesDataDTO {
  identifier: string;
  jobGroup: string;
  measuredEvent: string;
  data: TimeSeriesDataPointDTO[];

  constructor (dto: TimeSeriesDataDTO) {
    this.identifier = dto.identifier;
    this.jobGroup = dto.jobGroup;
    this.measuredEvent = dto.measuredEvent;
    this.data = dto.data;
  }
}
