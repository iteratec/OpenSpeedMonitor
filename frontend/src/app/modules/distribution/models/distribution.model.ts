export interface DistributionDTO {
  identifier: string;
  label: string;
  jobGroup: string;
  measurand: string;
  page: string;
  data: number[];
  median: number;
}

export class Distribution implements DistributionDTO {
  identifier: string;
  label: string;
  jobGroup: string;
  measurand: string;
  page: string;
  data: number[];
  median: number;

  constructor(dto: DistributionDTO) {
    this.identifier = dto.identifier;
    this.label = dto.label;
    this.jobGroup = dto.jobGroup;
    this.measurand = dto.measurand;
    this.page = dto.page;
    this.data = dto.data;
    this.median = dto.median;
  }
}
