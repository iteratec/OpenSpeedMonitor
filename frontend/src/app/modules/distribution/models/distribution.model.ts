import {DistributionPointDTO} from './distribution-point.model';

export interface DistributionDTO {
  identifier: string;
  jobGroup: string;
  measuredEvent: string;
  data: DistributionPointDTO[];
}

export class Distribution implements DistributionDTO {
  identifier: string;
  jobGroup: string;
  measuredEvent: string;
  data: DistributionPointDTO[];

  constructor(dto: DistributionDTO) {
    this.identifier = dto.identifier;
    this.jobGroup = dto.jobGroup;
    this.measuredEvent = dto.measuredEvent;
    this.data = dto.data;
  }
}
