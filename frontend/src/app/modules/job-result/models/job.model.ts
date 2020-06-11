export interface Job {
  id: number;
  label: string;
  script: string;
  location: any;
  lastRun: Date;
  lastChange: Date;
  description: string;
}
