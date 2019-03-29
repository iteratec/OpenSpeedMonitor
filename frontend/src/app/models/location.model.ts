import {SelectableBrowser} from "./browser.model";

export interface SelectableLocationDTO {
  id: number;
  name: string;
  parent: SelectableBrowser;
}

export class SelectableLocation implements SelectableLocationDTO {
  id: number;
  name: string;
  parent: SelectableBrowser;

  constructor (dto: SelectableLocationDTO) {
    this.id = dto.id;
    this.name = dto.name;
    this.parent = dto.parent;
  }
}
