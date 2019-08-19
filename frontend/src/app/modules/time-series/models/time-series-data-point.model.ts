export interface TimeSeriesDataPointDTO {
  date: Date;
  value: number;
  agent: string;
}

export class TimeSeriesDataPoint implements TimeSeriesDataPointDTO {
  date: Date;
  value: number;
  agent: string;

  constructor (dto: TimeSeriesDataPointDTO) {
    this.date = dto.date;
    this.value = dto.value;
    this.agent = dto.agent;
  }
}
