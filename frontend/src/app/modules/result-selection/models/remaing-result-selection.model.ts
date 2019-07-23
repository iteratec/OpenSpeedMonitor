export interface RemainingResultSelectionDTO {
  fromComparative?: Date;
  toComparative?: Date;
  measurands?: string[];
  performanceAspects?: string[];
  deviceTypes?: string[];
  operatingSystems?: string[];
}

export class RemainingResultSelection implements RemainingResultSelectionDTO {
  fromComparative?: Date;
  toComparative?: Date;
  measurands?: string[];
  performanceAspects?: string[];
  deviceTypes?: string[];
  operatingSystems?: string[];

  constructor (dto: RemainingResultSelectionDTO) {
    this.fromComparative = dto.fromComparative;
    this.toComparative = dto.toComparative;
    this.measurands = dto.measurands;
    this.performanceAspects = dto.performanceAspects;
    this.deviceTypes = dto.deviceTypes;
    this.operatingSystems = dto.operatingSystems;
  }
}

export enum RemainingResultSelectionParameter {
  MEASURANDS = "measurands",
  PERFORMANCE_ASPECTS = "performanceAspects",
  DEVICE_TYPES = "deviceTypes",
  OPERATING_SYSTEMS = "operatingSystems"
}
