export interface ConnectivityDTO {
  id: number;
  name: string;
}

export class Connectivity implements ConnectivityDTO {
  id: number;
  name: string;

  constructor (dto: ConnectivityDTO) {
    this.id = dto.id;
    this.name = dto.name;
  }
}
