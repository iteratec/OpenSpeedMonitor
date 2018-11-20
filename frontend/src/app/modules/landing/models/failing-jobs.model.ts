export interface FailingJobDTO {
  job_id: number;
  percentageFailLast5: number;
  location: string;
  application: string;
  script: string;
  browser: string;
}

export class FailingJob implements FailingJobDTO {
  application: string;
  job_id: number;
  percentageFailLast5: number;
  location: string;
  script: string;
  browser: string;

  constructor(dto: FailingJobDTO) {
    this.job_id = dto.job_id;
    this.percentageFailLast5 = dto.percentageFailLast5;
    this.location = dto.location;
    this.application = dto.application;
    this.script = dto.script;
    this.browser = dto.browser;
  }
}
