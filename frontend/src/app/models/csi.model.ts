import {parseDate} from "../utils/date.util";

export interface CsiDTO {
  date?: string | Date,
  csiDocComplete?: number
}

export class Csi implements CsiDTO {
  date: Date;
  csiDocComplete: number;

  constructor(dto: CsiDTO) {
    this.date = parseDate(dto.date || new Date(0));
    this.csiDocComplete = dto.csiDocComplete;
  }
}
