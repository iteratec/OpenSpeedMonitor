export interface FailingJobStatisticDTO {
  minimumFailedJobSuccessRate: number;
  numberOfFailingJobs: number;
}

export class FailingJobStatistic implements FailingJobStatisticDTO {
  minimumFailedJobSuccessRate: number;
  numberOfFailingJobs: number;

  constructor(dto: FailingJobStatisticDTO) {
    this.minimumFailedJobSuccessRate = dto.minimumFailedJobSuccessRate;
    this.numberOfFailingJobs = dto.numberOfFailingJobs;
  }
}
