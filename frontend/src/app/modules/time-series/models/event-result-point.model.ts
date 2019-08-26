export interface EventResultPointDTO {
  date: Date;
  value: number;
  agent: string;
}

export class EventResultPoint implements EventResultPointDTO {
  date: Date;
  value: number;
  agent: string;

  constructor(dto: EventResultPointDTO) {
    this.date = dto.date;
    this.value = dto.value;
    this.agent = dto.agent;
  }
}
