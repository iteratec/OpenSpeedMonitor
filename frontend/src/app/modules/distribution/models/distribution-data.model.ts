import {DistributionDTO} from './distribution.model';

export interface DistributionDataDTO {
  series: DistributionDTO[];
}

export class EventResultData implements DistributionDataDTO {
  series: DistributionDTO[];

  constructor() {
    this.series = [];
  }
}
