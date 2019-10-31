import {DistributionPointDTO} from './distribution-point.model';

export interface DistributionDTO {
  identifier: string;
  jobGroup: string;
  measurand: string;

  //TODO temp?
  data: number[];
}

export class Distribution implements DistributionDTO {
  identifier: string;
  jobGroup: string;
  measurand: string;

  //TODO temp?
  data: number[];

  constructor(dto: DistributionDTO) {
    this.identifier = dto.identifier;
    this.jobGroup = dto.jobGroup;
    this.measurand = dto.measurand;
    this.data = dto.data;
  }
}
