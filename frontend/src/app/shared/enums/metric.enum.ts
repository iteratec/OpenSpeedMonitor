import {Unit} from "./unit.enum";

export class Metric {
  constructor(public name: string, public unit: Unit, public icon: string) {
  }
}

export const Metrics = {
  "SPEED_INDEX": new Metric("SpeedIndex", Unit.SECONDS, 'far fa-eye'),
  "DOCUMENT_COMPLETE": new Metric("Document Complete", Unit.SECONDS, 'fas fa-stopwatch'),
  "BYTES_FULLY_LOADED": new Metric("Bytes Fully Loaded", Unit.MEBIBYTES, 'fas fa-weight-hanging')
};


