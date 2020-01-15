import {EventResultSeriesDTO} from './event-result-series.model';
import {SummaryLabel} from "./summary-label.model";

export interface EventResultDataDTO {
  series: EventResultSeriesDTO[];
  summaryLabels: SummaryLabel[];
}

export class EventResultData implements EventResultDataDTO {
  series: EventResultSeriesDTO[];
  summaryLabels: SummaryLabel[];

  constructor() {
    this.series = [];
    this.summaryLabels = [];
  }
}
