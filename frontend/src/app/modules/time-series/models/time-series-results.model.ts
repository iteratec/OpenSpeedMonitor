import {TimeSeriesDataDTO} from './time-series-data.model';

export interface TimeSeriesResultsDTO {
  series: TimeSeriesDataDTO[];
}

export class TimeSeriesResults implements TimeSeriesResultsDTO {
  series: TimeSeriesDataDTO[];

  constructor () {
    this.series = [];
  }
}
