export class EventDTO {
  eventDate: Date;
  description: string;
  shortName: string;
}

export class TimeEvent extends EventDTO {
  eventDate: Date;
  description: string;
  shortName: string;

  constructor(eventDate: Date, description: string, shortName: string) {
    super();
    this.eventDate = eventDate;
    this.description = description;
    this.shortName = shortName;
  }
}
