import {parseDate} from "../utils/date.util";

export interface ApplicationDTO {
  id: number;
  name: string;
  pageCount?: number;
  dateOfLastResults?: string | Date;
  csiConfigurationId?: number;
}

export class Application implements ApplicationDTO {
  id: number;
  name: string;
  pageCount: number;
  dateOfLastResults: Date;
  csiConfigurationId: number;

  constructor(dto: ApplicationDTO) {
    this.id = dto.id;
    this.name = dto.name;
    this.pageCount = isNaN(dto.pageCount) ? null : dto.pageCount;
    this.dateOfLastResults = dto.dateOfLastResults ? parseDate(dto.dateOfLastResults) : null;
    this.csiConfigurationId = dto.csiConfigurationId || null;
  }
}
