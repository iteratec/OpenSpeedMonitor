/**
 * Created by glastra on 27.06.18.
 */

import {MeasuredEvent} from "./measured-event.model"
import {Measurand} from "./measurand.model"

export type Threshold = {
  id: number;
  lowerBoundary: number;
  measurand: Measurand;
  measuredEvent: MeasuredEvent;
  upperBoundary: number;
  edit: boolean;
  saved: boolean;

}
