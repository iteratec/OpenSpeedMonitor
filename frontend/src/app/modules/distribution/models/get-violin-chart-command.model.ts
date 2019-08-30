export interface GetViolinchartCommandDTO {
  preconfiguredDashboard?: number;
  from: Date;
  to: Date;
  interval: number;
  measurand: string;
  jobGroups: number[];
  pages?: number[];
  measuredEvents?: number[];
  browsers?: number[];
  locations?: number[];
  connectivities?: number[];
  deviceTypes?: string[];
  operatingSystems?: string[];
}

export class GetViolinchartCommand implements GetViolinchartCommandDTO {
  preconfiguredDashboard?: number;
  from: Date;
  to: Date;
  interval: number;
  measurand: string;
  jobGroups: number[];
  pages?: number[];
  measuredEvents?: number[];
  browsers?: number[];
  locations?: number[];
  connectivities?: number[];
  deviceTypes?: string[];
  operatingSystems?: string[];

  constructor(dto: GetViolinchartCommandDTO) {
    this.preconfiguredDashboard = dto.preconfiguredDashboard;
    this.from = dto.from;
    this.to = dto.to;
    this.interval = dto.interval;
    this.measurand = dto.measurand;
    this.jobGroups = dto.jobGroups;
    this.pages = dto.pages;
    this.measuredEvents = dto.measuredEvents;
    this.browsers = dto.browsers;
    this.locations = dto.locations;
    this.connectivities = dto.connectivities;
    this.deviceTypes = dto.deviceTypes;
    this.operatingSystems = dto.operatingSystems;
  }
}
