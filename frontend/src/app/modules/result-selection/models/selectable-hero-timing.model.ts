export interface SelectableHeroTimingDTO {
  id: number;
  name: string;
}

export class SelectableHeroTiming implements SelectableHeroTimingDTO {
  id: number;
  name: string;

  constructor (dto: SelectableHeroTimingDTO) {
    this.id = dto.id;
    this.name = dto.name;
  }
}
