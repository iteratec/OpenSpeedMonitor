export interface JobResultFilter {
  from: Date;
  to: Date;
  testAgent: string;
  jobResultStatus: (string | object)[];
  wptStatus: (string | object)[];
  description: string;
}
