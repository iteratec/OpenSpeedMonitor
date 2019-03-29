export interface BrowserDTO {
  id: number;
  name: string;
}

export class Browser implements BrowserDTO {
  id: number;
  name: string;

  constructor (dto: BrowserDTO) {
    this.id = dto.id;
    this.name = dto.name;
  }
}
