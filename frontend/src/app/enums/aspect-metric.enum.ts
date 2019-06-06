import {Unit} from "./unit.enum";

export class AspectMetric {
  constructor(public identifier: string, public name: string,  public unit: Unit, public icon: string) {
  }
}

export const AspectMetrics = {
  PAGE_CONSTRUCTION_STARTED: new AspectMetric('PAGE_CONSTRUCTION_STARTED', 'Is it happening?', Unit.SECONDS, 'fas fa-hourglass-start'),
  PAGE_SHOWS_USEFUL_CONTENT: new AspectMetric('PAGE_SHOWS_USEFUL_CONTENT', 'Is it useful?', Unit.SECONDS, 'fas fa-eye'),
  CONSISTENTLY_INTERACTIVE: new AspectMetric('PAGE_IS_USABLE', 'Is it usable?', Unit.SECONDS, 'fas fa-hand-pointer')
};
