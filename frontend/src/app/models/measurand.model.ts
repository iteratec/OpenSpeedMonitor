import {Loading} from "./loading.model";
import {PerformanceAspectType} from "./perfomance-aspect.model";

export interface SelectableMeasurand {
  kind: "selectable-measurand"
  name: string
  id: string
  isUserTiming?: boolean
}

export interface MeasurandGroup extends Loading {
  name: string,
  values: SelectableMeasurand[]
}

export type Measurand = PerformanceAspectType | SelectableMeasurand
