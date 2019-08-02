export interface GetLinechartCommandDTO {
  preconfiguredDashboard?: number;
  from: Date;
  to: Date;
  interval: number;
  measurands: string[];
  applications: number[];
  pages?: number[];
  measuredEvents?: number[];
  browsers?: number[];
  locations?: number[];
  connectivities?: number[];
  deviceTypes?: string[];
  operatingSystems?: string[];
}

export class GetLinechartCommand implements GetLinechartCommandDTO {
  preconfiguredDashboard?: number;
  from: Date;
  to: Date;
  interval: number;
  measurands: string[];
  applications: number[];
  pages?: number[];
  measuredEvents?: number[];
  browsers?: number[];
  locations?: number[];
  connectivities?: number[];
  deviceTypes?: string[];
  operatingSystems?: string[];

  constructor (dto: GetLinechartCommandDTO) {
    this.preconfiguredDashboard = dto.preconfiguredDashboard;
    this.from = dto.from;
    this.to = dto.to;
    this.interval = dto.interval;
    this.measurands = dto.measurands;
    this.applications = dto.applications;
    this.pages = dto.pages;
    this.measuredEvents = dto.measuredEvents;
    this.browsers = dto.browsers;
    this.locations = dto.locations;
    this.connectivities = dto.connectivities;
    this.deviceTypes = dto.deviceTypes;
    this.operatingSystems = dto.operatingSystems;
  }
}
