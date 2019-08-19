import {LineChartDataPointDTO} from './line-chart-data-value.model';

/**
 * Representation of on point on the x-axis (key) with all data for the y-axis (values).
 */
export interface LineChartDataDTO {
  key: string;
  values: LineChartDataPointDTO[];
}

export class LineChartData implements LineChartDataDTO {
  key: string;
  values: LineChartDataPointDTO[];
}
