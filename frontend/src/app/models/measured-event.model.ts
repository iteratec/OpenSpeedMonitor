import { Page } from "./page.model";

export interface MeasuredEventDTO {
  id: number;
  name: string;
  parent: Page;
}

export class MeasuredEvent implements MeasuredEventDTO {
  id: number;
  name: string;
  parent: Page;

  constructor (dto: MeasuredEventDTO) {
    this.id = dto.id;
    this.name = dto.name;
    this.parent = dto.parent;
  }
}
