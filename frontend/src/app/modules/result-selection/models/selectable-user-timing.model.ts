export interface SelectableUserTimingDTO {
  id: number;
  name: string;
}

export class SelectableUserTiming implements SelectableUserTimingDTO {
  id: number;
  name: string;

  constructor (dto: SelectableUserTimingDTO) {
    this.id = dto.id;
    this.name = dto.name;
  }
}
