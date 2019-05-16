import {Browser} from "./browser.model";

export interface LocationDTO {
  id: number;
  name: string;
  parent: Browser;
}

export class Location implements LocationDTO {
  id: number;
  name: string;
  parent: Browser;

  constructor (dto: LocationDTO) {
    this.id = dto.id;
    this.name = dto.name;
    this.parent = dto.parent;
  }
}
