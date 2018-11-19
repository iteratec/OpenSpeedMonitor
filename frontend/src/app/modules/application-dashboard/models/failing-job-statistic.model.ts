export interface FailingJobStatisticDTO {
  minimumFailedJobSuccessRate: number;
  numberOfFailingJobs: number;
  percentage?: number;
  name?: string;
  id?: number;
}

export class FailingJobStatistic implements FailingJobStatisticDTO {
  minimumFailedJobSuccessRate: number;
  numberOfFailingJobs: number;
  percentage?: number;
  name?: string;
  id?: number;

  constructor(dto: FailingJobStatisticDTO) {
    this.minimumFailedJobSuccessRate = dto.minimumFailedJobSuccessRate;
    this.numberOfFailingJobs = dto.numberOfFailingJobs;
    this.percentage = (100 - dto.minimumFailedJobSuccessRate / dto.numberOfFailingJobs);
  }
}
