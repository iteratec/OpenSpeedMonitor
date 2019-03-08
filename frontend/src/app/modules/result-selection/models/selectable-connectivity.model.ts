export interface SelectableConnectivityDTO {
  id: number;
  name: string;
}

export class SelectableConnectivity implements SelectableConnectivityDTO {
  id: number;
  name: string;

  constructor (dto: SelectableConnectivityDTO) {
    this.id = dto.id;
    this.name = dto.name;
  }
}
