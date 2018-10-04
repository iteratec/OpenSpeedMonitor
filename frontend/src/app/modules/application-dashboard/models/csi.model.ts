import {parseDate} from "../../../utils/date.util";

export interface CsiDTO {
  date: string | Date,
  csiDocComplete: number,
  csiVisComplete: number
}

export class Csi implements CsiDTO {
  date: Date;
  csiDocComplete: number;
  csiVisComplete: number;

  constructor(dto: CsiDTO) {
    this.date = parseDate(dto.date);
    this.csiDocComplete = dto.csiDocComplete;
    this.csiVisComplete = dto.csiVisComplete;
  }
}
