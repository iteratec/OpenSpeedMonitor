import {EventResultSeriesDTO} from './event-result-series.model';
import {SummaryLabel} from "./summary-label.model";

export interface EventResultDataDTO {
  measurandGroups: any;
  series: EventResultSeriesDTO[];
  summaryLabels: SummaryLabel[];
  numberOfTimeSeries: number;
}

export class EventResultData implements EventResultDataDTO {
  measurandGroups: any;
  series: EventResultSeriesDTO[];
  summaryLabels: SummaryLabel[];
  numberOfTimeSeries: number;

  constructor() {
    this.measurandGroups = {};
    this.series = [];
    this.summaryLabels = [];
    this.numberOfTimeSeries = 0;
  }
}
