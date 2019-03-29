import {SelectablePage} from "../modules/result-selection/models/selectable-page.model";

export interface SelectableMeasuredEventDTO {
  id: number;
  name: string;
  parent: SelectablePage;
}

export class SelectableMeasuredEvent implements SelectableMeasuredEventDTO {
  id: number;
  name: string;
  parent: SelectablePage;

  constructor (dto: SelectableMeasuredEventDTO) {
    this.id = dto.id;
    this.name = dto.name;
    this.parent = dto.parent;
  }
}
