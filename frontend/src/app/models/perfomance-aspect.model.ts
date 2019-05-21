import {SelectableMeasurand} from "./measurand.model";


export interface PerformanceAspect {
  id: number
  pageId: number
  jobGroupId: number
  browserId: number
  measurand: SelectableMeasurand
  performanceAspectType: PerformanceAspectType
  persistent: boolean
}

export interface PerformanceAspectType {
  name: string
  icon: string
}
