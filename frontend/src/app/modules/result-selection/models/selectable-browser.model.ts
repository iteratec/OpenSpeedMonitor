export interface SelectableBrowserDTO {
  id: number;
  name: string;
}

export class SelectableBrowser implements SelectableBrowserDTO {
  id: number;
  name: string;

  constructor (dto: SelectableBrowserDTO) {
    this.id = dto.id;
    this.name = dto.name;
  }
}
