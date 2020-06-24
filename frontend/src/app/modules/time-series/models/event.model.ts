export interface EventDTO {
  eventDate: Date;
  description: string;
  shortName: string;
}

export class TimeEvent implements EventDTO {
  eventDate: Date;
  description: string;
  shortName: string;

  constructor(eventDate: Date, description: string, shortName: string) {
    this.eventDate = eventDate;
    this.description = description;
    this.shortName = shortName;
  }
}
