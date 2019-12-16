/**
 * Representation of one point on the y-axis (value) with additional informations.
 */
export class TimeSeriesPoint {
  date: Date;
  value: number;
  tooltipText: string;

  constructor() {}

  public equals(other: TimeSeriesPoint) {
    return other && this.date.getTime() == other.date.getTime() && this.tooltipText == other.tooltipText && this.value == other.value;
  }
}
