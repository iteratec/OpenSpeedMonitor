export interface PageDTO {
  name: string
  id: number

}
export class Page implements PageDTO {
  id: number;
  name: string;

  constructor (dto: PageDTO) {
    this.id = dto.id;
    this.name = dto.name;
  }
}
