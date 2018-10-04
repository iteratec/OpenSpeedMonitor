import {parseDate} from "../utils/date.util";

export interface ApplicationDTO {
  id: number;
  name: string;
  numPages?: number;
  dateOfLastResults: string | Date;
  csiConfigurationId: number;
}

export class Application implements ApplicationDTO {
  id: number;
  name: string;
  numPages: number;
  dateOfLastResults: Date;
  csiConfigurationId: number;

  constructor(dto: ApplicationDTO) {
    this.id = dto.id;
    this.name = dto.name;
    this.numPages = isNaN(dto.numPages) ? null : dto.numPages;
    this.dateOfLastResults = parseDate(dto.dateOfLastResults);
    this.csiConfigurationId = dto.csiConfigurationId;
  }
}
