import {TimeSeriesPoint} from './time-series-point.model';

/**
 * Representation of on point on the x-axis (key) with all data for the y-axis (values).
 */
export class TimeSeries {
  key: string;
  values: TimeSeriesPoint[];

  constructor() {
    this.key = "";
    this.values = [new TimeSeriesPoint()];
  }
}
