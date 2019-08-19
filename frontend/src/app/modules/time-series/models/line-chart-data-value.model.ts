/**
 * Representation of one point on the y-axis (value) with additional informations.
 */
export interface LineChartDataPointDTO {
  date: Date;
  value: number;
  tooltipText: string;
}

export class LineChartDataPoint implements LineChartDataPointDTO {
  date: Date;
  value: number;
  tooltipText: string;

  constructor() {}
}
