export interface DistributionPointDTO {
  date: Date;
  value: number;
  agent: string;
}

export class DistributionPoint implements DistributionPointDTO {
  date: Date;
  value: number;
  agent: string;

  constructor(dto: DistributionPointDTO) {
    this.date = dto.date;
    this.value = dto.value;
    this.agent = dto.agent;
  }
}
