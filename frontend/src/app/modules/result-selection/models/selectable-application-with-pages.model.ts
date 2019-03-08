import {SelectableApplication, SelectableApplicationDTO} from "./selectable-application.model";
import {SelectablePage} from "./selectable-page.model";

export interface SelectableApplicationWithPagesDTO extends SelectableApplicationDTO {
  pages: SelectablePage[];
}

export class SelectableApplicationWithPages implements SelectableApplicationWithPagesDTO {
  id: number;
  name: string;
  pages: SelectablePage[];

  constructor(dto: SelectableApplicationWithPagesDTO) {
    this.id = dto.id;
    this.name = dto.name;
    this.pages = dto.pages;
  }
}
