export interface SelectablePageDTO {
  id: number;
  name: string;
}

export class SelectablePage implements SelectablePageDTO {
  id: number;
  name: string;

  constructor (dto: SelectablePageDTO) {
    this.id = dto.id;
    this.name = dto.name;
  }
}
