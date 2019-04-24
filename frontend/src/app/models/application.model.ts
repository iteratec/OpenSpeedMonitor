import {parseDate} from "../utils/date.util";
import { Page } from "./page.model";

export interface ApplicationDTO {
  id: number;
  name: string;
  pageCount?: number;
  dateOfLastResults?: string | Date;
  csiConfigurationId?: number;
  pages?: Page[];
  tags?: Array<string>;
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

export class SelectableApplication implements ApplicationDTO {
  id: number;
  name: string;
  tags: Array<string>;

  constructor(dto: ApplicationDTO) {
    this.id = dto.id;
    this.name = dto.name;
    this.tags = dto.tags;
  }
}

export class ApplicationWithPages implements ApplicationDTO {
  id: number;
  name: string;
  pages: Page[];

  constructor(dto: ApplicationDTO) {
    this.id = dto.id;
    this.name = dto.name;
    this.pages = dto.pages;
  }
}
