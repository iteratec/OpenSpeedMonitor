export interface GetEventResultDataCommandDTO {
  preconfiguredDashboard?: number;
  from: Date;
  to: Date;
  interval: number;
  measurands?: string[];
  jobGroups: number[];
  pages?: number[];
  measuredEvents?: number[];
  browsers?: number[];
  locations?: number[];
  connectivities?: number[];
  deviceTypes?: string[];
  operatingSystems?: string[];
  performanceAspectTypes?: string[];
}

export class GetEventResultDataCommand implements GetEventResultDataCommandDTO {
  preconfiguredDashboard?: number;
  from: Date;
  to: Date;
  interval: number;
  measurands?: string[];
  jobGroups: number[];
  pages?: number[];
  measuredEvents?: number[];
  browsers?: number[];
  locations?: number[];
  connectivities?: number[];
  deviceTypes?: string[];
  operatingSystems?: string[];
  performanceAspectTypes?: string[];

  constructor(dto: GetEventResultDataCommandDTO) {
    this.preconfiguredDashboard = dto.preconfiguredDashboard;
    this.from = dto.from;
    this.to = dto.to;
    this.interval = dto.interval;
    this.measurands = dto.measurands;
    this.jobGroups = dto.jobGroups;
    this.pages = dto.pages;
    this.measuredEvents = dto.measuredEvents;
    this.browsers = dto.browsers;
    this.locations = dto.locations;
    this.connectivities = dto.connectivities;
    this.deviceTypes = dto.deviceTypes;
    this.operatingSystems = dto.operatingSystems;
    this.performanceAspectTypes = dto.performanceAspectTypes;
  }
}
