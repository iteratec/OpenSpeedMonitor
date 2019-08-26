export interface RemainingResultSelectionDTO {
  interval?: number;
  fromComparative?: Date;
  toComparative?: Date;
  measurands?: string[];
  performanceAspectTypes?: string[];
  deviceTypes?: string[];
  operatingSystems?: string[];
}

export class RemainingResultSelection implements RemainingResultSelectionDTO {
  interval?: number;
  fromComparative?: Date;
  toComparative?: Date;
  measurands?: string[];
  performanceAspectTypes?: string[];
  deviceTypes?: string[];
  operatingSystems?: string[];

  constructor (dto: RemainingResultSelectionDTO) {
    this.interval = dto.interval;
    this.fromComparative = dto.fromComparative;
    this.toComparative = dto.toComparative;
    this.measurands = dto.measurands;
    this.performanceAspectTypes = dto.performanceAspectTypes;
    this.deviceTypes = dto.deviceTypes;
    this.operatingSystems = dto.operatingSystems;
  }
}

export enum RemainingResultSelectionParameter {
  MEASURANDS = "measurands",
  PERFORMANCE_ASPECT_TYPES = "performanceAspectTypes",
  DEVICE_TYPES = "deviceTypes",
  OPERATING_SYSTEMS = "operatingSystems"
}
