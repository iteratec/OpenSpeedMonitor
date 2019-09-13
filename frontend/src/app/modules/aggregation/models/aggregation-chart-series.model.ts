export interface AggregationChartSeriesDTO {
  measurand: string;
  aggregationValue: string | number;
  jobGroup: string;
  measurandGroup: string;
  measurandLabel: string;
  page: string;
  value: number;
  unit: string;
  sideLabel?: string;
  valueComparative?: number;
  browser?: string;
  deviceType?: string;
  operatingSystem?: string;
  isImprovement ?: boolean;
  isDeterioration ?: boolean;
}

export class AggregationChartSeries implements AggregationChartSeriesDTO {
  measurand: string;
  aggregationValue: string | number;
  jobGroup: string;
  measurandGroup: string;
  measurandLabel: string;
  page: string;
  value: number;
  unit: string;
  sideLabel?: string;
  browser?: string;
  deviceType?: string;
  valueComparative?: number;
  operatingSystem?: string;
  isImprovement ?: boolean;
  isDeterioration ?: boolean;

  constructor(dto: AggregationChartSeriesDTO) {
    this.measurand = dto.measurand;
    this.measurandLabel = dto.measurandLabel;
    this.measurandGroup = dto.measurandGroup;
    this.browser = dto.browser;
    this.deviceType = dto.deviceType;
    this.jobGroup = dto.jobGroup;
    this.operatingSystem = dto.operatingSystem;
    this.page = dto.page;
    this.value = dto.value;
    this.valueComparative = dto.valueComparative;
    this.aggregationValue = dto.aggregationValue;
    this.unit = dto.unit;
    this.sideLabel = dto.sideLabel;
    this.isDeterioration = dto.isDeterioration;
    this.isImprovement = dto.isImprovement;
  }
}

