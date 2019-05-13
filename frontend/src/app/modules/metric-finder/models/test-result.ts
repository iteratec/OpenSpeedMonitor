
export class TestInfo {

  constructor(
    public testId: string,
    public run: number,
    public cached: boolean,
    public step: number,
    public wptUrl: string
  ) {
  }
}

export class TestResult {
  constructor(
    public date: Date,
    public testInfo: TestInfo,
    public timings: {[metric: string]: number} = {}
  ) {
  }
}
