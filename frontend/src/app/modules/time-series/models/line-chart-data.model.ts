import {LineChartDataPoint} from './line-chart-data-value.model';

/**
 * Representation of on point on the x-axis (key) with all data for the y-axis (values).
 */
export class LineChartData {
  key: string;
  values: LineChartDataPoint[];

  constructor() {
    this.key = "";
    this.values = [new LineChartDataPoint()];
  }
}
