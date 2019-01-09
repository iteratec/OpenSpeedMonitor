export interface GraphiteServerDTO {
  id: number;
  address: string;
  port: number;
  protocol: string;
  webAppAddress: string;
  prefix: string;
}

export class GraphiteServer implements GraphiteServerDTO {
  id: number;
  address: string;
  port: number;
  protocol: string;
  webAppAddress: string;
  prefix: string;

  constructor(dto: GraphiteServerDTO) {
    this.id = dto.id;
    this.address = dto.address;
    this.port = dto.port;
    this.protocol = dto.protocol;
    this.webAppAddress = dto.webAppAddress;
    this.prefix = dto.prefix;
  }

}
