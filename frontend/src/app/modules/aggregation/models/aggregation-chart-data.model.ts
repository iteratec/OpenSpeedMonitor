import {AggregationChartSeries} from "./aggregation-chart-series.model";

export interface AggregationChartDataDTO {
  measurand: string;
  aggregationValue: string | number;
  label: string;
  measurandGroup: string;
  unit: string;
  highlighted: boolean;
  selected: boolean;
  isDeterioration?: boolean;
  isImprovement?: boolean;
  hasComparative: boolean;
  color: string;
  series: AggregationChartSeries[];
}

export class AggregationChartData implements AggregationChartDataDTO {
  measurand: string;
  aggregationValue: string | number;
  label: string;
  measurandGroup: string;
  unit: string;
  highlighted: boolean;
  selected: boolean;
  isDeterioration?: boolean;
  isImprovement?: boolean;
  hasComparative: boolean;
  color: string;
  series: AggregationChartSeries[];

  constructor(dto: AggregationChartDataDTO) {
    this.measurand = dto.measurand;
    this.label = dto.label;
    this.measurandGroup = dto.measurandGroup;
    this.unit = dto.unit;
    this.highlighted = dto.highlighted;
    this.selected = dto.selected;
    this.isDeterioration = dto.isDeterioration;
    this.isImprovement = dto.isImprovement;
    this.hasComparative = dto.hasComparative;
    this.color = dto.color;
    this.series = dto.series;
    this.aggregationValue = dto.aggregationValue;
  }
}

export interface AggregationChartDataByMeasurand {
  [measurand: string]: AggregationChartData;
}

