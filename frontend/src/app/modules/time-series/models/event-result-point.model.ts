import {WptInfo} from "./wpt-info.model";

export interface EventResultPointDTO {
  date: Date;
  value: number;
  agent: string;
  wptInfo: WptInfo;
}

export class EventResultPoint implements EventResultPointDTO {
  date: Date;
  value: number;
  agent: string;
  wptInfo: WptInfo;

  constructor(dto: EventResultPointDTO) {
    this.date = dto.date;
    this.value = dto.value;
    this.agent = dto.agent;
    this.wptInfo = dto.wptInfo;
  }
}
