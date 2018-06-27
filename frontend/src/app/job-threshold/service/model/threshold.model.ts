/**
 * Created by glastra on 27.06.18.
 */

import {MeasuredEventForScript} from "./measured-events-for-script.model"
import {Measurand} from "./measurand.model"

export type ThresholdForJob = {
  id: number;
  lowerBoundary: number;
  measurand: Measurand;
  measuredEvent: MeasuredEventForScript;
  upperBoundary: number;

}
