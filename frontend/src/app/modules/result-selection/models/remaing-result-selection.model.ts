export interface RemainingResultSelectionDTO {
  fromComparative?: Date;
  toComparative?: Date;
  measurands?: string[];
  deviceTypes?: string[];
  operatingSystems?: string[];
}

export class RemainingResultSelection implements RemainingResultSelectionDTO {
  fromComparative?: Date;
  toComparative?: Date;
  measurands?: string[];
  deviceTypes?: string[];
  operatingSystems?: string[];

  constructor (dto: RemainingResultSelectionDTO) {
    this.fromComparative = dto.fromComparative;
    this.toComparative = dto.toComparative;
    this.measurands = dto.measurands;
    this.deviceTypes = dto.deviceTypes;
    this.operatingSystems = dto.operatingSystems;
  }
}

export enum RemainingResultSelectionParameter {
  MEASURANDS = "measurands",
  DEVICE_TYPES = "deviceTypes",
  OPERATING_SYSTEMS = "operatingSystems"
}
