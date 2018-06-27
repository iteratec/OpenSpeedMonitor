/**
 * Created by glastra on 27.06.18.
 */
import {MeasuredEventForScript} from "./measured-events-for-script.model"
import {Threshold} from "./threshold.model"

export type ThresholdForJob = {
  measuredEvent: MeasuredEventForScript;
  thresholds: Threshold[];
}
