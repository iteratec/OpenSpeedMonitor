import {Loading} from "./loading.model";

export interface SelectableMeasurand {
  name: string
  id: string
}

export interface MeasurandGroup extends  Loading {
  name: string,
  values: SelectableMeasurand[]
}
