import {parseDate} from "../../../utils/date.util";

export interface ApplicationDTO {
  id: number;
  name: string;
  csi: number;
  numPages: number;
  csiDate: string | Date;
  lastResultDate: string | Date;
}

export class Application implements ApplicationDTO {
  id: number;
  name: string;
  csi: number;
  numPages: number;
  csiDate: Date;
  lastResultDate: Date;

  constructor(dto: ApplicationDTO) {
    this.id = dto.id;
    this.name = dto.name;
    this.csi = dto.csi;
    this.numPages = dto.numPages;
    this.csiDate = parseDate(dto.csiDate);
    this.lastResultDate = parseDate(dto.lastResultDate);
  }
}
