export interface GetBarchartCommandDTO {
  from?: Date;
  to?: Date;
  fromComparative?: Date;
  toComparative?: Date;
  pages?: number[];
  jobGroups?: number[];
  measurands?: string[];
  browsers?: number[];
  deviceTypes?: string[];
  operatingSystems?: string[];
  aggregationValue?: string | number;
}

export class GetBarchartCommand implements GetBarchartCommandDTO {
  from: Date;
  to: Date;
  fromComparative?: Date;
  toComparative?: Date;
  pages?: number[];
  jobGroups?: number[];
  measurands?: string[];
  browsers?: number[];
  deviceTypes?: string[];
  operatingSystems?: string[];
  aggregationValue?: string | number;

  constructor (dto: GetBarchartCommandDTO) {
    this.from = dto.from;
    this.to = dto.to;
    this.fromComparative = dto.fromComparative;
    this.toComparative = dto.toComparative;
    this.pages = dto.pages;
    this.jobGroups = dto.jobGroups;
    this.measurands = dto.measurands;
    this.browsers = dto.browsers;
    this.deviceTypes = dto.deviceTypes;
    this.operatingSystems = dto.operatingSystems;
    this.aggregationValue = dto.aggregationValue;
  }
}

export class RemainingGetBarchartCommand implements GetBarchartCommandDTO {
  fromComparative?: Date;
  toComparative?: Date;
  measurands?: string[];
  deviceTypes?: string[];
  operatingSystems?: string[];

  constructor (dto: GetBarchartCommandDTO) {
    this.fromComparative = dto.fromComparative;
    this.toComparative = dto.toComparative;
    this.measurands = dto.measurands;
    this.deviceTypes = dto.deviceTypes;
    this.operatingSystems = dto.operatingSystems;
  }
}

export enum GetBarchartCommandParameter {
  MEASURANDS = "measurands",
  DEVICE_TYPES = "deviceTypes",
  OPERATING_SYSTEMS = "operatingSystems"
}
