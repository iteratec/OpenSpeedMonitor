import {parseDate} from '../../../utils/date.util';

export interface TestInfoDTO {
  testId: string;
  run: number;
  cached: boolean;
  step: number;
  wptUrl: string;
}

export class TestInfo implements TestInfoDTO {
  public testId: string;
  public run: number;
  public cached: boolean;
  public step: number;
  public wptUrl: string;

  constructor(dto: TestInfoDTO) {
    this.testId = dto.testId;
    this.run = dto.run;
    this.cached = dto.cached;
    this.step = dto.step;
    this.wptUrl = dto.wptUrl;
  }
}

export declare interface TimingsMap {
  [metric: string]: number;
}

export interface TestResultDTO {
  date: string | Date;
  testInfo: TestInfoDTO;
  timings: TimingsMap;
}

export class TestResult implements  TestResultDTO {
  public date: Date;
  public testInfo: TestInfo;
  public timings: TimingsMap = {};
  public readonly id;

  constructor(dto: TestResultDTO) {
    this.date = parseDate(dto.date);
    this.timings = dto.timings;
    this.testInfo = new TestInfo(dto.testInfo);
    this.id = [this.testInfo.testId, this.testInfo.run, this.testInfo.cached ? 1 : 0, this.testInfo.step].join('_');
  }
}
