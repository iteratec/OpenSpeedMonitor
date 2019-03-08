export interface SelectableApplicationDTO {
  id: number;
  name: string;
}

export class SelectableApplication implements SelectableApplicationDTO {
  id: number;
  name: string;

  constructor(dto: SelectableApplicationDTO) {
    this.id = dto.id;
    this.name = dto.name;
  }
}
