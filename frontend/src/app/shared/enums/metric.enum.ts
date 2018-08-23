import {Unit} from "./unit.enum";

export class Metrics {
  SPEED_INDEX: Metric = new Metric("SpeedIndex", Unit.SECONDS, 'far fa-eye');
  DOCUMENT_COMPLETE: Metric = new Metric("Document Complete", Unit.SECONDS, 'fas fa-stopwatch');
  BYTES_FULLY_LOADED: Metric = new Metric("Bytes Fully Loaded", Unit.MEBIBYTES, 'fas fa-weight-hanging');

  constructor() {
  }

  static getMetrics(): Metrics {
    return new Metrics();
  }
}

export class Metric {
  constructor(public name: string, public unit: Unit, public icon: string) {
  }
}
